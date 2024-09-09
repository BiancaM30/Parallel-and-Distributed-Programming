$noProcesses = $args[0] # No of processes
$noRuns = $args[1] # No of runs

$suma = 0

for($i = 0; $i -lt $noRuns; $i++){
    Write-Host "Rulare" ($i+1)
    $a = mpiexec -n $noProcesses Lab3-PPD.exe
    Write-Host $a
    $suma += $a
}

$media = $suma/$noRuns
Write-Host "Timp executie mediu: " $media