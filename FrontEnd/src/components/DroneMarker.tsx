import type { Drone } from '../types';
import './DroneMarker.css';
import { Crosshair, BatteryFull, BatteryMedium, BatteryLow } from 'lucide-react';

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
        <Crosshair size={24} className="drone-icon" />
      </div>
      <div className={`drone-battery ${batteryClass}`}>
        <BatteryIcon size={12} />
        <span>{bateriaPercent.toFixed(0)}%</span>
      </div>
    </div>
  );
}
