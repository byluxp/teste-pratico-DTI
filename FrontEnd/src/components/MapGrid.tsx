import { useState, useRef, type MouseEvent as ReactMouseEvent, useEffect } from 'react';
import type { Drone, Pedido, Obstaculo } from '../types';
import DroneMarker from './DroneMarker';
import './MapGrid.css';
import { MapPin, X, ZoomIn, ZoomOut, ArrowUp, ArrowDown, ArrowLeft, ArrowRight, Move, Zap } from 'lucide-react';

interface MapGridProps {
  obstaculos: Obstaculo[];
  drones: Drone[];
  pedidos: Pedido[];
  activeVoos: any[];
  onAddObstaculo: (obs: Obstaculo) => void;
  onDeleteObstaculo: (id: number) => void;
}

export default function MapGrid({ obstaculos, drones, pedidos, activeVoos, onAddObstaculo, onDeleteObstaculo }: MapGridProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapGridRef = useRef<HTMLDivElement>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [startPoint, setStartPoint] = useState<{ x: number; y: number } | null>(null);
  const [currentPoint, setCurrentPoint] = useState<{ x: number; y: number } | null>(null);
  const [pendingObstaculo, setPendingObstaculo] = useState<Obstaculo | null>(null);
  const [animatedDrones, setAnimatedDrones] = useState<Record<number, {x: number, y: number, status: string}>>({});
  
  // Camera controls
  const [manualZoom, setManualZoom] = useState(1);
  const [panX, setPanX] = useState(0);
  const [panY, setPanY] = useState(0);

  // Helper para detectar interseção com obstáculo e calcular desvio
  const getLineCircleIntersection = (x1: number, y1: number, x2: number, y2: number, cx: number, cy: number, r: number) => {
    const dx = x2 - x1;
    const dy = y2 - y1;
    const lenSq = dx * dx + dy * dy;
    if (lenSq === 0) return null;

    let t = ((cx - x1) * dx + (cy - y1) * dy) / lenSq;
    t = Math.max(0, Math.min(1, t)); // clamp

    const projX = x1 + t * dx;
    const projY = y1 + t * dy;

    const distSq = (cx - projX) ** 2 + (cy - projY) ** 2;
    if (distSq < r * r) {
      return { projX, projY };
    }
    return null;
  };

  const getPath = (x1: number, y1: number, x2: number, y2: number, obsList: Obstaculo[], depth = 0): {x: number, y: number}[] => {
    if (depth > 5) return [{ x: x2, y: y2 }];
    
    for (const obs of obsList) {
      const cx = obs.coordenadaX;
      const cy = obs.coordenadaY;
      const r = obs.raioKm + 3; // +3 de margem segura

      const intersection = getLineCircleIntersection(x1, y1, x2, y2, cx, cy, r);
      if (intersection) {
        const { projX, projY } = intersection;
        let vx = projX - cx;
        let vy = projY - cy;
        let vLen = Math.sqrt(vx * vx + vy * vy);
        
        if (vLen === 0) {
          vx = -(y2 - y1);
          vy = (x2 - x1);
          vLen = Math.sqrt(vx * vx + vy * vy);
          if (vLen === 0) { vx = 1; vy = 0; vLen = 1; }
        }
        
        // Ponto de desvio (waypoint)
        const wx = cx + (vx / vLen) * r;
        const wy = cy + (vy / vLen) * r;
        
        const path1 = getPath(x1, y1, wx, wy, obsList, depth + 1);
        const path2 = getPath(wx, wy, x2, y2, obsList, depth + 1);
        
        return [...path1, ...path2];
      }
    }
    return [{ x: x2, y: y2 }];
  };

  useEffect(() => {
    if (activeVoos && activeVoos.length > 0) {
      activeVoos.forEach(voo => {
        const droneId = voo.drone.id;
        const baseX = voo.drone.baseX ?? 0;
        const baseY = voo.drone.baseY ?? 0;
        const pts = voo.pedidos;
        if (!pts || pts.length === 0) return;

        let sequence: any[] = [];
        let curX = baseX;
        let curY = baseY;

        // Start
        sequence.push({ x: curX, y: curY, status: 'CARREGANDO', delay: 1000 });
        
        // Deliveries
        pts.forEach((p: any) => {
          const path = getPath(curX, curY, p.coordenadaX, p.coordenadaY, obstaculos);
          
          for (let i = 0; i < path.length - 1; i++) {
            sequence.push({ x: path[i].x, y: path[i].y, status: 'EM_VOO', delay: 1000 });
          }
          
          const finalWp = path[path.length - 1];
          sequence.push({ x: finalWp.x, y: finalWp.y, status: 'EM_VOO', delay: 1500 });
          sequence.push({ x: finalWp.x, y: finalWp.y, status: 'ENTREGANDO', delay: 1000 });
          
          curX = p.coordenadaX;
          curY = p.coordenadaY;
        });

        // Return (cada drone retorna à sua própria base)
        const returnPath = getPath(curX, curY, baseX, baseY, obstaculos);
        for (let i = 0; i < returnPath.length - 1; i++) {
          sequence.push({ x: returnPath[i].x, y: returnPath[i].y, status: 'RETORNANDO', delay: 1000 });
        }
        const finalRet = returnPath[returnPath.length - 1];
        sequence.push({ x: finalRet.x, y: finalRet.y, status: 'RETORNANDO', delay: 1500 });
        sequence.push({ x: baseX, y: baseY, status: 'IDLE', delay: 500 });

        let currentStep = 0;
        const playNext = () => {
          if (currentStep >= sequence.length) return;
          const step = sequence[currentStep];
          setAnimatedDrones(prev => ({
            ...prev,
            [droneId]: { x: step.x, y: step.y, status: step.status }
          }));
          currentStep++;
          setTimeout(playNext, step.delay);
        };
        playNext();
      });
    }
  }, [activeVoos]);

  // Constants for map scale (0 to 100)
  const MAP_MAX = 100;

  const getCoordinates = (e: ReactMouseEvent) => {
    if (!mapGridRef.current) return { x: 0, y: 0 };
    const rect = mapGridRef.current.getBoundingClientRect();
    let x = ((e.clientX - rect.left) / rect.width) * MAP_MAX;
    let y = ((e.clientY - rect.top) / rect.height) * MAP_MAX;
    x = Math.max(0, Math.min(100, x));
    y = Math.max(0, Math.min(100, y));
    return { x, y };
  };

  const handleMouseDown = (e: ReactMouseEvent) => {
    if (pendingObstaculo || obstaculos.length >= 5) return; 
    const coords = getCoordinates(e);
    setStartPoint(coords);
    setCurrentPoint(coords);
    setIsDrawing(true);
  };

  const handleMouseMove = (e: ReactMouseEvent) => {
    if (!isDrawing) return;
    setCurrentPoint(getCoordinates(e));
  };

  const handleMouseUp = () => {
    if (!isDrawing || !startPoint || !currentPoint) return;
    setIsDrawing(false);

    const cx = (startPoint.x + currentPoint.x) / 2;
    const cy = (startPoint.y + currentPoint.y) / 2;
    const dx = startPoint.x - currentPoint.x;
    const dy = startPoint.y - currentPoint.y;
    const radius = Math.sqrt(dx * dx + dy * dy) / 2; 

    // Max size of 30% of map (15% radius)
    const clampedRadius = Math.min(radius, 15);

    if (clampedRadius > 2) { 
      setPendingObstaculo({
        coordenadaX: cx,
        coordenadaY: cy,
        raioKm: clampedRadius
      });
    } else {
      setStartPoint(null);
      setCurrentPoint(null);
    }
  };

  const confirmObstaculo = () => {
    if (pendingObstaculo) {
      onAddObstaculo(pendingObstaculo);
    }
    setPendingObstaculo(null);
    setStartPoint(null);
    setCurrentPoint(null);
  };

  const cancelObstaculo = () => {
    setPendingObstaculo(null);
    setStartPoint(null);
    setCurrentPoint(null);
  };

  // Calculate center camera based on drone position (or base if none moving)
  let cameraX = 0;
  let cameraY = 0;
  let isMoving = false;

  const firstMovingDrone = Object.values(animatedDrones).find(d => d.status !== 'IDLE' && d.status !== 'CARREGANDO');
  if (firstMovingDrone) {
    cameraX = firstMovingDrone.x;
    cameraY = firstMovingDrone.y;
    isMoving = true;
  }

  let finalScale = manualZoom;
  let finalPanX = panX;
  let finalPanY = panY;

  if (isMoving) {
    finalScale = 1.2;
    finalPanX = 50 - cameraX;
    finalPanY = 50 - cameraY;
  }
  
  // Apply scale and translate
  const transform = `scale(${finalScale}) translate(${finalPanX}%, ${finalPanY}%)`;

  return (
    <div 
      className="map-grid-wrapper"
      ref={containerRef}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
    >
      <div className="map-controls-panel">
        <button onClick={() => setPanY(y => y + 15)} title="Mover para Cima"><ArrowUp size={12} /></button>
        <div style={{ display: 'flex', gap: '3px' }}>
          <button onClick={() => setPanX(x => x + 15)} title="Mover para Esquerda"><ArrowLeft size={12} /></button>
          <button onClick={() => { setManualZoom(1); setPanX(0); setPanY(0); }} title="Resetar Câmera"><Move size={12} /></button>
          <button onClick={() => setPanX(x => x - 15)} title="Mover para Direita"><ArrowRight size={12} /></button>
        </div>
        <button onClick={() => setPanY(y => y - 15)} title="Mover para Baixo"><ArrowDown size={12} /></button>
        <div style={{ display: 'flex', gap: '3px', marginTop: '6px' }}>
          <button onClick={() => setManualZoom(z => Math.min(z + 0.2, 3))} title="Zoom In"><ZoomIn size={12} /></button>
          <button onClick={() => setManualZoom(z => Math.max(z - 0.2, 0.5))} title="Zoom Out"><ZoomOut size={12} /></button>
        </div>
      </div>

      <div 
        className="map-grid" 
        ref={mapGridRef}
        style={{ transform, transition: 'transform 0.5s ease', transformOrigin: 'center' }}
      >
        {/* Draw Base Stations (uma para cada drone, em sua coordenada de origem) */}
        {drones.map(d => (
          <div
            key={`base-${d.id}`}
            className="base-station"
            style={{ top: `${d.baseY ?? 0}%`, left: `${d.baseX ?? 0}%` }}
          >
            {d.codigo ?? `DD${d.id}`}
          </div>
        ))}
        {drones.map(d => {
          const animState = animatedDrones[d.id!];
          const status = animState ? animState.status : d.status;
          const isCharging = status === 'CARREGANDO' || status === 'IDLE' || status === 'RECARREGANDO';
          return (
            <div
              key={`charger-${d.id}`}
              className={`base-charger ${isCharging ? 'active' : ''}`}
              title={`Base de carregamento ${d.codigo ?? ''}`}
              style={{ top: `${d.baseY ?? 0}%`, left: `${d.baseX ?? 0}%` }}
            >
              <Zap size={14} />
            </div>
          );
        })}

        {/* Draw Pedidos as Pins */}
        {pedidos.map(p => (
          <div key={p.id} className="pedido-pin" style={{ top: `${p.coordenadaY}%`, left: `${p.coordenadaX}%` }}>
            <MapPin color={p.status === 'ENTREGUE' ? 'var(--purple)' : 'var(--yellow)'} size={20} />
            <span className="pin-id">#{p.id}</span>
          </div>
        ))}

        {/* Draw Obstáculos */}
        {obstaculos.map((obs, i) => (
          <div 
            key={obs.id || i} 
            className="obstaculo-zone"
            style={{ 
              top: `${obs.coordenadaY}%`, 
              left: `${obs.coordenadaX}%`,
              width: `${obs.raioKm * 2}%`,
              height: `${obs.raioKm * 2}%`
            }}
          >
            {obs.id && (
              <button 
                className="btn-delete-obs" 
                onClick={(e) => { e.stopPropagation(); onDeleteObstaculo(obs.id!); }}
              >
                <X size={12} />
              </button>
            )}
          </div>
        ))}

        {/* Draw Current Selection Box (and pending state) */}
        {(isDrawing || pendingObstaculo) && startPoint && currentPoint && (
          <>
            <div 
              className="selection-box"
              style={{
                top: `${Math.min(startPoint.y, currentPoint.y)}%`,
                left: `${Math.min(startPoint.x, currentPoint.x)}%`,
                width: `${Math.abs(startPoint.x - currentPoint.x)}%`,
                height: `${Math.abs(startPoint.y - currentPoint.y)}%`,
              }}
            />
            {pendingObstaculo && (
              <div 
                className="confirm-box" 
                style={{
                  top: `${Math.max(startPoint.y, currentPoint.y) + 2}%`,
                  left: `${Math.min(startPoint.x, currentPoint.x)}%`,
                }}
              >
                <span>Criar Zona Bloqueada?</span>
                <div className="confirm-actions">
                  <button className="btn-confirm-yes" onClick={confirmObstaculo}>Confirmar</button>
                  <button className="btn-confirm-no" onClick={cancelObstaculo}>Cancelar</button>
                </div>
              </div>
            )}
          </>
        )}

        {/* Draw Drones */}
        {drones.map(d => {
          const animState = animatedDrones[d.id!];
          // Prioriza a animação local (waypoints com desvio de obstáculos) quando ativa;
          // caso contrário, usa a posição em tempo real recebida via SSE (posX/posY), atualizada a cada 200ms no backend.
          const x = animState ? animState.x : (d.posX ?? d.baseX ?? 0);
          const y = animState ? animState.y : (d.posY ?? d.baseY ?? 0);
          
          // Merge real backend drone with animated status
          const displayDrone = animState 
            ? { ...d, status: animState.status as any } 
            : d;

          return <DroneMarker key={d.id} drone={displayDrone} x={x} y={y} />;
        })}
      </div>
    </div>
  );
}
