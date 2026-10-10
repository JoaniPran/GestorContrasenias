Option Explicit

Dim shell, fileSystem, scriptDirectory, launcher, exitCode

Set shell = CreateObject("WScript.Shell")
Set fileSystem = CreateObject("Scripting.FileSystemObject")
scriptDirectory = fileSystem.GetParentFolderName(WScript.ScriptFullName)
launcher = """" & scriptDirectory & "\ejecutar-windows.bat" & """"

exitCode = shell.Run(launcher, 0, True)
If exitCode <> 0 Then
    MsgBox "KeyVault no pudo iniciarse. Ejecuta ejecutar-windows.bat desde una terminal para ver el error.", _
        vbExclamation, "KeyVault"
End If
