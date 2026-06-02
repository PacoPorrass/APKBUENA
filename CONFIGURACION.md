# VaultDrive — Configuración completada

## Datos de Azure ya configurados en el código:
- **Client ID**: f76b6c71-4faf-49b8-8f52-aa3efab90c69
- **Tenant ID**: ce41ecbd-c265-4b29-b346-e21bacf98945
- **Redirect URI**: msauth://com.empresa.vaultdrive/gi1mL6gXo8UOoPxVgGvd6zEA

## Lo único que falta en Azure Portal:

### 1. Permisos de API — añadir estos si no están:
Ve a tu app en Azure → **Permisos de API** → **Agregar permiso** → **Microsoft Graph** → **Delegados**:
- `Files.ReadWrite`
- `Files.ReadWrite.All`
- `User.Read`
- `Sites.Read.All` ← necesario para carpetas compartidas
Pulsa **"Conceder consentimiento de administrador"**

### 2. Que usuarios normales (sin rol admin) puedan usar la app:
Ve a **Azure Active Directory** → **Aplicaciones empresariales** → busca **VaultDrive**
→ **Propiedades** → cambia **"¿Se requiere asignación de usuario?"** a **No**

### 3. Autenticación → Plataformas → Android:
- Nombre del paquete: `com.empresa.vaultdrive`
- Hash de firma: `gi1mL6gXo8UOoPxVgGvd6zEA`

## Compilar y subir a GitHub Actions:
```bash
git init
git add .
git commit -m "VaultDrive v2"
git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
git push -u origin main
```
Luego en GitHub → Actions → espera el tick verde → descarga la APK.
