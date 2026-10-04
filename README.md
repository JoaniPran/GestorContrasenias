# KeyVault — gestor local de contraseñas

## Seguridad y migración

- Las nuevas cuentas requieren una contraseña maestra de al menos 12 caracteres.
- Las contraseñas maestras se guardan como hashes PBKDF2-HMAC-SHA256 con sal aleatoria (600.000 iteraciones).
- La bóveda usa AES-256-GCM con nonce aleatorio, sal por bóveda y verificación de integridad.
- Los archivos se escriben de forma atómica y, en sistemas POSIX, se restringen al propietario (`rw-------`).
- En el siguiente inicio de sesión, las cuentas y bóvedas del formato anterior se migran automáticamente. La migración conserva el formato original si falla antes de completarse.

Antes de actualizar, cierra el programa y haz una copia de seguridad de `usuarios.csv` y de cada archivo `contrasenas_<usuario>.csv`. No borres esos archivos: contienen las cuentas y bóvedas.

Los datos se guardan en el directorio de trabajo desde el que se ejecuta la aplicación. La protección de archivos depende también de la seguridad de la cuenta y del dispositivo donde se ejecuta KeyVault.

## Generar una distribución portable en Linux

Se necesita Maven y un JDK que incluya `jpackage`. Desde la raíz del proyecto, ejecuta:

```sh
mvn clean package
mkdir -p target/portable-input portable
cp target/gestor-contrasenas-1.0.0.jar target/portable-input/

jpackage \
	--type app-image \
	--name KeyVault \
	--app-version 1.0.0 \
	--input target/portable-input \
	--main-jar gestor-contrasenas-1.0.0.jar \
	--main-class gestorcontrasenas.GestorContrasenas \
	--dest portable
```

El resultado queda en `portable/KeyVault/` e incluye un runtime de Java, por lo que no hace falta instalar Java en la máquina donde se ejecuta. Para iniciarlo desde una terminal:

```sh
cd portable/KeyVault
./bin/KeyVault
```

KeyVault guarda sus archivos en el directorio de trabajo. Para que los datos viajen con la distribución, copia `usuarios.csv` y `contrasenas_<usuario>.csv` dentro de `portable/KeyVault/` **antes del primer inicio**. No compartas la distribución si contiene esos archivos: son privados. La carpeta `portable/` está excluida de Git.

Este procedimiento genera una aplicación portable para Linux y la arquitectura del equipo donde se ejecuta. Para Windows o macOS, genera el paquete en el sistema correspondiente.
