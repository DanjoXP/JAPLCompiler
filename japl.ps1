# JAPL convenient CLI launcher
$jarPath = Join-Path $PSScriptRoot "japl.jar"
java -jar $jarPath @args
