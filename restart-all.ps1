<# Restart both backend and frontend to apply changes #>
Write-Host 'Restarting World Conflict Monitor (backend + frontend)...' -ForegroundColor Cyan

# Stop common dev processes
Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force
Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*worldconflict-api*' } | Stop-Process -Force

# Start backend
Write-Host 'Starting backend...' -ForegroundColor Green
Start-Process -WorkingDirectory 'C:\Users\victor\Desktop\pruebasIaAgent\pruebas11\backend' -FilePath 'powershell.exe' -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-Command','& "C:\Users\victor\Desktop\pruebasIaAgent\pruebas11\run-backend.ps1"'

# Start frontend
Write-Host 'Starting frontend...' -ForegroundColor Green
Start-Process -WorkingDirectory 'C:\Users\victor\Desktop\pruebasIaAgent\pruebas11\frontend' -FilePath 'powershell.exe' -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-Command','npm run dev'

Write-Host 'Done. WebApp should be available at http://localhost:8080 and http://localhost:5173' -ForegroundColor Yellow
