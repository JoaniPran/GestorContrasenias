# Seguseña — gestor local de contraseñas

## Seguridad y migración

- Las nuevas cuentas requieren una contraseña maestra de al menos 12 caracteres.
- El nombre de usuario tiene entre 1 y 64 caracteres. Puede usar letras; @, números, puntos, guiones y guiones bajos son opcionales.
- Las contraseñas maestras se guardan como hashes PBKDF2-HMAC-SHA256 con sal aleatoria (600.000 iteraciones).
- La bóveda usa AES-256-GCM con nonce aleatorio, sal por bóveda y verificación de integridad.
- Las contraseñas copiadas se borran del portapapeles del sistema después de 30 segundos o al cerrar la aplicación/sesión, solo si el contenido copiado por Seguseña no fue reemplazado antes.
- Los archivos se escriben de forma atómica y, en sistemas POSIX, se restringen al propietario (`rw-------`).
- En Windows se excluye la ventana principal de las capturas compatibles; si no se puede activar esa protección, las contraseñas permanecen ocultas. En Linux, las contraseñas reveladas se vuelven a ocultar al perder el foco. Estas medidas dependen del sistema y no impiden todos los métodos de captura.
- En el siguiente inicio de sesión, las cuentas y bóvedas del formato anterior se migran automáticamente. La migración conserva el formato original si falla antes de completarse.
- Cada bóveda usa ahora un nombre de archivo codificado y único por usuario. Los archivos antiguos se migran al iniciar sesión si el nombre heredado no está compartido; si es ambiguo, la aplicación bloquea esa bóveda y conserva el original para evitar mezclar cuentas.

Antes de actualizar, cierra el programa y haz una copia de seguridad de `usuarios.csv`, `preferencias.properties` y todos los archivos `contrasenas_*.csv`. No borres esos archivos: contienen las cuentas, preferencias y bóvedas.

Los datos se guardan en el directorio de trabajo desde el que se ejecuta la aplicación. La protección de archivos depende también de la seguridad de la cuenta y del dispositivo donde se ejecuta Seguseña.

## Usar Seguseña localmente

Necesitas Java 8 o posterior. Para compilar desde el código fuente también necesitas Maven. Abre una terminal en la carpeta del proyecto y ejecuta el script correspondiente; si todavía no existe el JAR, el script lo compila automáticamente:

**Linux:**

```sh
sh ./ejecutar.sh
```

**Windows (sin mostrar la consola):** haz doble clic en `ejecutar-windows.vbs`.

Si necesitas ver los mensajes de compilación o diagnóstico, ejecútalo desde PowerShell o el Símbolo del sistema:

```powershell
./ejecutar-windows.bat
```

También puedes compilar y ejecutar el JAR manualmente en cualquier sistema con Java y Maven:

```sh
mvn package
java -jar target/segusena-1.0.1.jar
```

Al iniciar, crea una cuenta con **Registrar** (usuario y contraseña maestra de al menos 12 caracteres), luego inicia sesión. Puedes marcar **Recordar usuario** para completar el nombre de usuario en próximos inicios; la contraseña maestra nunca se recuerda. Las contraseñas de tus servicios se agregan desde el formulario principal; puedes buscarlas, editarlas y eliminarlas desde allí. El botón **Importar Google** permite seleccionar o arrastrar un CSV de Google Password Manager; valida las columnas `name`, `url`, `username`, `password` y `note`, importa nombre, usuario y contraseña, y reemplaza la contraseña cuando ya existe el mismo servicio y usuario. El icono de engranaje permite cambiar el tema claro/oscuro, que se guarda por usuario.

Los archivos `usuarios.csv`, `contrasenas_*.csv` y `preferencias.properties` se crean en el directorio desde el que se ejecuta la aplicación. No los compartas; haz copias de seguridad privadas si necesitas conservar tus cuentas y bóvedas.

## Generar una distribución portable en Linux

Se necesita Maven y un JDK que incluya `jpackage`. Desde la raíz del proyecto, ejecuta:

```sh
mvn clean package
mkdir -p target/portable-input portable
cp target/segusena-1.0.1.jar target/portable-input/

jpackage \
	--type app-image \
	--name Segusena \
	--app-version 1.0.1 \
	--input target/portable-input \
	--icon assets/shield-hd.png \
	--main-jar segusena-1.0.1.jar \
	--main-class gestorcontrasenas.GestorContrasenas \
	--dest portable
```

Maven genera un JAR autocontenido con las bibliotecas de la aplicación, que `jpackage` incluye en la distribución. El resultado queda en `portable/Segusena/` e incluye también un runtime de Java, por lo que no hace falta instalar Java en la máquina donde se ejecuta. Para iniciarlo desde una terminal:

```sh
cd portable/Segusena
./bin/Segusena
```

Seguseña guarda sus archivos en el directorio de trabajo. Para que los datos viajen con la distribución, copia `usuarios.csv`, `preferencias.properties` y todos los archivos `contrasenas_*.csv` dentro de `portable/Segusena/` **antes del primer inicio**. No compartas la distribución si contiene esos archivos: son privados. La carpeta `portable/` está excluida de Git.

Este procedimiento genera una aplicación portable para Linux y la arquitectura del equipo donde se ejecuta.

## Generar una distribución portable en Windows

En Windows, instala Maven y un JDK 16 o posterior que incluya `jpackage`. Abre PowerShell en la carpeta del proyecto y ejecuta:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-windows.ps1
```

El paquete se genera en `portable\Segusena\` e incluye las bibliotecas de la aplicación y su propio runtime de Java. Para iniciarlo sin mostrar una consola, abre `portable\Segusena\Segusena.vbs` o `Segusena.exe`; el generador ya no crea un lanzador `.bat`. Si quieres conservar datos existentes, copia `usuarios.csv`, `preferencias.properties` y todos los archivos `contrasenas_*.csv` a `portable\Segusena\` antes del primer inicio. No compartas esos archivos: son privados.

El paquete de Windows debe generarse en Windows (y para la arquitectura de Windows de destino); `jpackage` no crea el ejecutable de Windows desde Linux. La aplicación también puede ejecutarse desde el JAR con Java instalado, usando `ejecutar-windows.bat`.
