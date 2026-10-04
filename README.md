# KeyVault — gestor local de contraseñas

## Seguridad y migración

- Las nuevas cuentas requieren una contraseña maestra de al menos 12 caracteres.
- Las contraseñas maestras se guardan como hashes PBKDF2-HMAC-SHA256 con sal aleatoria (600.000 iteraciones).
- La bóveda usa AES-256-GCM con nonce aleatorio, sal por bóveda y verificación de integridad.
- Los archivos se escriben de forma atómica y, en sistemas POSIX, se restringen al propietario (`rw-------`).
- En el siguiente inicio de sesión, las cuentas y bóvedas del formato anterior se migran automáticamente. La migración conserva el formato original si falla antes de completarse.

Antes de actualizar, cierra el programa y haz una copia de seguridad de `usuarios.csv` y de cada archivo `contrasenas_<usuario>.csv`. No borres esos archivos: contienen las cuentas y bóvedas.

Los datos se guardan en el directorio de trabajo desde el que se ejecuta la aplicación. La protección de archivos depende también de la seguridad de la cuenta y del dispositivo donde se ejecuta KeyVault.
