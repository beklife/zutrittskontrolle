# Server einrichten (Beispiel: kostenlose Oracle-Cloud-VM)

Diese Anleitung bringt die Demo auf einen frischen Linux-Server (Ubuntu 24.04, ARM oder x86).

## 1. Server und Domain

1. Oracle Cloud: **Compute → Instances → Create**. Image **Ubuntu 24.04**, Shape **VM.Standard.A1.Flex** (Ampere, z. B. 2 OCPU / 12 GB).
   Den eigenen öffentlichen SSH-Schlüssel hochladen.
2. Im **Virtual Cloud Network → Security List** zwei Ingress-Regeln anlegen: TCP **80** und TCP **443** von `0.0.0.0/0`.
3. Kostenlose Subdomain bei [duckdns.org](https://www.duckdns.org) anlegen und auf die **öffentliche IP** der VM zeigen lassen.

## 2. Server vorbereiten

```bash
ssh ubuntu@<ÖFFENTLICHE-IP>

# Oracle-Ubuntu-Images blockieren Ports per iptables – 80/443 freigeben und speichern
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save

# Docker installieren
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
exit   # neu einloggen, damit die Gruppe wirkt
```

## 3. Traefik starten (einmal pro Server)

```bash
ssh ubuntu@<ÖFFENTLICHE-IP>
git clone https://github.com/beklife/zutrittskontrolle.git
cd zutrittskontrolle/deploy/traefik
cp .env.example .env && nano .env          # ACME_EMAIL eintragen
docker network create traefik
docker compose up -d
```

## 4. Anwendung starten

```bash
cd ~/zutrittskontrolle/deploy
cp .env.example .env && nano .env          # DOMAIN=<name>.duckdns.org, TRAEFIK_NETWORK=traefik,
                                           # CERT_RESOLVER=letsencrypt, langes DB-Passwort
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps   # alle "healthy"?
```

Danach ist die Demo unter `https://<name>.duckdns.org` erreichbar. Das Zertifikat holt Traefik beim ersten Aufruf automatisch.

## Update nach neuem Push

```bash
cd ~/zutrittskontrolle && git pull
cd deploy && docker compose -f docker-compose.prod.yml up -d --build
```
