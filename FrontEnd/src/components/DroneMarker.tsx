import type { Drone } from '../types';
import './DroneMarker.css';
import { BatteryFull, BatteryMedium, BatteryLow } from 'lucide-react';

interface DroneMarkerProps {
  drone: Drone;
  x: number;
  y: number;
}

export default function DroneMarker({ drone, x, y }: DroneMarkerProps) {
  // Convert coordinates to percentages for absolute positioning
  // Assuming grid is 100x100 for simplicity
  const top = `${(y / 100) * 100}%`;
  const left = `${(x / 100) * 100}%`;

  const bateriaPercent = (drone.autonomiaAtualKm / drone.autonomiaMaximaKm) * 100;
  
  let BatteryIcon = BatteryFull;
  let batteryClass = "batt-high";
  if (bateriaPercent < 50) {
    BatteryIcon = BatteryMedium;
    batteryClass = "batt-med";
  }
  if (bateriaPercent < 20) {
    BatteryIcon = BatteryLow;
    batteryClass = "batt-low";
  }

  return (
    <div className="drone-marker" style={{ top, left }}>
      <div className="drone-icon-wrapper">
        <svg viewBox="0 0 64 64" className="drone-svg" aria-label="Drone">
          <rect x="20" y="22" width="24" height="16" rx="4" fill="currentColor" />
          <rect x="14" y="16" width="8" height="8" rx="2" fill="currentColor" />
          <rect x="42" y="16" width="8" height="8" rx="2" fill="currentColor" />
          <rect x="14" y="40" width="8" height="8" rx="2" fill="currentColor" />
          <rect x="42" y="40" width="8" height="8" rx="2" fill="currentColor" />
          <path d="M26 14h12" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
          <path d="M28 50h8" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
        </svg>
      </div>
      <div className={`drone-battery ${batteryClass}`}>
        <BatteryIcon size={12} />
        <span>{bateriaPercent.toFixed(0)}%</span>
      </div>
    </div>
  );
}
