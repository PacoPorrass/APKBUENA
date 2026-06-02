# VaultDrive — Manual de Usuario
**Versión 1.0 | App Android para OneDrive Personal**

---

## ¿Qué es VaultDrive?
VaultDrive es una app Android para acceder, organizar y subir archivos a tu OneDrive desde el móvil.
No necesitas instalar el cliente oficial de OneDrive. Solo necesitas tu cuenta Microsoft (Outlook, Hotmail).

### ¿Para qué sirve?
- Ver y navegar carpetas de tu OneDrive
- Subir fotos, vídeos y documentos desde la galería
- Crear subcarpetas directamente en OneDrive
- Fijar una carpeta favorita para acceso rápido con un toque

### ¿Qué NO hace?
- No descarga archivos al móvil
- No edita documentos

---

## Requisitos
| Campo | Valor |
|-------|-------|
| Android | 8.0 (Oreo) o superior |
| Cuenta | Microsoft personal (Outlook, Hotmail) |
| Internet | WiFi o datos móviles |
| Espacio | ~30 MB |

---

## Instalación
1. Copia el APK al móvil (USB, WhatsApp, email)
2. Abre el archivo desde el explorador de archivos
3. Si aparece "fuentes desconocidas" → Ajustes → Seguridad → activar permiso
4. Pulsa Instalar

---

## Primer uso — Iniciar sesión
1. Pulsa **"Iniciar sesión con Microsoft"**
2. Introduce tu email y contraseña de Microsoft
3. Acepta los permisos (leer/escribir OneDrive, ver perfil)
4. La sesión se guarda cifrada — no tendrás que volver a hacer login

> VaultDrive nunca guarda tu contraseña. Solo guarda un token temporal cifrado con AES-256.

---

## Pantalla principal
- **Barra superior**: carpeta actual, tu nombre, botón cerrar sesión
- **⚡ Chip**: acceso rápido a la carpeta fijada
- **Lista**: carpetas arriba, archivos debajo, ordenados alfabéticamente
- **Botón 📁+**: crear nueva carpeta
- **Botón ⬆️**: subir archivos desde galería

---

## Navegar carpetas
- **Entrar**: pulsa sobre cualquier carpeta
- **Volver**: flecha ← en la barra superior o botón atrás del móvil
- **Actualizar**: desliza la lista hacia abajo (pull to refresh)

---

## Subir archivos
1. Entra en la carpeta destino
2. Pulsa el botón **⬆️** azul
3. Permite acceso a la galería si te lo pide
4. Selecciona uno o varios archivos
5. La barra de progreso muestra el avance

**Formatos compatibles**: JPG, PNG, MP4, PDF, DOC, DOCX, XLS, XLSX, ZIP y más.

---

## Crear subcarpetas
1. Navega hasta la ubicación deseada
2. Pulsa **📁+**
3. Escribe el nombre y pulsa **Crear**

---

## Carpeta fijada — Acceso rápido
1. Mantén pulsada cualquier carpeta (pulsación larga)
2. Selecciona **"📌 Fijar acceso rápido"**
3. Aparece el chip **⚡** en la barra superior
4. Púlsalo en cualquier momento para ir directamente

---

## Cerrar sesión
Pulsa el icono 🚪 → confirmar → vuelves a la pantalla de login.

---

## Solución de problemas

| Problema | Solución |
|----------|----------|
| No puedo iniciar sesión | Comprueba conexión y que sea cuenta Microsoft personal |
| Carpeta vacía | Desliza hacia abajo para actualizar |
| Error al subir | Comprueba conexión y espacio en OneDrive |
| Sesión expirada | La app intentará renovar sola; si no, inicia sesión de nuevo |

---

## Seguridad
| Medida | Detalle |
|--------|---------|
| Token cifrado | AES-256-GCM con Android Keystore |
| Sin contraseñas | Nunca se almacena la contraseña |
| HTTPS obligatorio | Toda comunicación cifrada |
| Sin backup | Datos no incluidos en copias de seguridad de Android |

---

*VaultDrive v1.0 — Acceso rápido y seguro a tu OneDrive*
