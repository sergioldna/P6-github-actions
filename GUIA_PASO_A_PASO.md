# 🚀 Guía Maestra: CI/CD con GitHub Actions, Docker y Kubernetes (ARC)

Esta guía detalla el proceso completo para desplegar la aplicación **IPS-Practica-Hotel** de forma automática. 

---

## 🧐 ¿Qué estamos haciendo? (Conceptos Teóricos)
El objetivo es que, cada vez que hagas un cambio en tu código y lo subas a GitHub (`git push`), ocurra lo siguiente:
1.  **CI (Integración Continua):** GitHub construye una imagen Docker de tu aplicación y la sube a Docker Hub.
2.  **CD (Despliegue Continuo):** Un "Runner" (un agente) que vive dentro de tu propio ordenador (en Minikube) se da cuenta de que hay una nueva versión y actualiza el despliegue en Kubernetes automáticamente.

---

## 🛠 Fase 1: Preparación de Credenciales

### 1.1. Docker Hub
Necesitas que GitHub pueda subir imágenes a tu cuenta de Docker Hub.
1.  Ve a [Docker Hub Settings -> Security](https://hub.docker.com/settings/security).
2.  Crea un **New Access Token** (llámalo `github-actions`).
3.  **Guarda el Token.**

### 1.2. GitHub Secrets
En tu repositorio de GitHub (`sergioldna/IPS-Practica-Hotel`), ve a **Settings -> Secrets and variables -> Actions** y añade:
*   `DOCKERHUB_USERNAME`: Tu usuario de Docker Hub.
*   `DOCKERHUB_TOKEN`: El token que acabas de copiar.
*   `REGISTRY`: `docker.io` (en la pestaña "Variables" en vez de "Secrets").

---

## 📦 Fase 2: Configuración de Kubernetes (Minikube)

### 2.1. Crear el Namespace
Kubernetes organiza las cosas en "habitaciones" llamadas namespaces. Usaremos `ips`.
```powershell
kubectl create namespace ips
```

### 2.2. Secreto para imágenes privadas
Para que Kubernetes pueda descargar tu imagen de Docker Hub, necesita permiso:
```powershell
kubectl create secret docker-registry mi-registro-secreto `
  --docker-server=https://index.docker.io/v1/ `
  --docker-username=TU_USUARIO `
  --docker-password=TU_TOKEN_DOCKERHUB `
  --docker-email=tu-email@ejemplo.com `
  --namespace=ips
```
*Nota: En PowerShell usamos el acento grave ( ` ) para saltar de línea.*

---

## 🤖 Fase 3: El Runner (ARC - Actions Runner Controller)

Aquí es donde instalamos el programa que "escucha" a GitHub dentro de tu Minikube.

### 3.1. Instalar el Controlador (Cerebro)
Usamos **Helm** (el instalador de apps para Kubernetes):
```powershell
helm install arc `
    --namespace ips `
    --create-namespace `
    oci://ghcr.io/actions/actions-runner-controller-charts/gha-runner-scale-set-controller
```

### 3.2. Crear el PAT de GitHub
El runner necesita un "DNI" para hablar con tu repo.
1.  Ve a [GitHub Settings -> Developer Settings -> Personal access tokens (classic)](https://github.com/settings/tokens).
2.  Genera un nuevo token con el permiso **`repo`**.
3.  Cópialo.

### 3.3. Instalar el Runner (Cuerpo)
```powershell
$GITHUB_PAT="tu_token_ghp_..."
$REPO_URL="https://github.com/sergioldna/IPS-Practica-Hotel"

helm install self-hosted `
    --namespace ips `
    --set githubConfigUrl=$REPO_URL `
    --set githubConfigSecret.github_token=$GITHUB_PAT `
    oci://ghcr.io/actions/actions-runner-controller-charts/gha-runner-scale-set
```

---

## 🔑 Fase 4: Permisos (RBAC) y Despliegue Inicial

### 4.1. Dar permisos al Runner
El runner por defecto no puede cambiar nada. Hay que darle permiso de "administrador" en el namespace `ips`.
Necesitas los archivos `runner-permissions.yml` y `runner-rolebinding.yml` y aplicarlos:
```powershell
kubectl apply -f runner-permissions.yml
kubectl apply -f runner-rolebinding.yml
```

### 4.2. Despliegue de la Aplicación
Crea los archivos `deployment.yml` (la app) y `service.yml` (para verla en el navegador) y aplícalos:
```powershell
kubectl apply -f deployment.yml
kubectl apply -f service.yml
```
*La app estará disponible en `http://localhost:30008` (si usas `minikube service hotel-service -n ips`).*

---

## 🔄 Fase 5: El Workflow (.github/workflows/docker-publish.yml)

Este archivo vive en tu código y le dice a GitHub qué hacer:
1.  **Job 1 (build-and-publish):** Se ejecuta en la nube de GitHub. Compila el Java, crea la imagen y la sube.
2.  **Job 2 (deploy-to-k8s):** Le dice a **TU** runner (el de Minikube): "Oye, actualiza la imagen del deployment con la nueva versión".

---

## 🛠 Troubleshooting Común en Windows/PowerShell
1.  **Error con `&&`:** En PowerShell no existe `&&`, usa `;`.
2.  **Error con `\`:** En PowerShell usa `` ` `` para continuar comandos en varias líneas.
3.  **No responde 'yes' en `auth can-i`:** Revisa que el nombre del `ServiceAccount` en el `RoleBinding` coincida exactamente con el que sale en `kubectl get sa -n ips`.
