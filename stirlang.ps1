# Stirlang CLI launcher
$jarPath = Join-Path $PSScriptRoot "stirlang.jar"
if ($args.Count -gt 0 -and ($args[0] -eq "-update" -or $args[0] -eq "--update" -or $args[0] -eq "update")) {
    & (Join-Path $PSScriptRoot "update.ps1")
    exit $LASTEXITCODE
}
& java -jar $jarPath @args
exit $LASTEXITCODE
