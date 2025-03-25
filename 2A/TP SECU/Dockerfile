# Utilise l'image officielle Debian comme base
FROM debian:latest

# Empêche les invites interactives pendant les installs
ENV DEBIAN_FRONTEND=noninteractive

# Met à jour les paquets et installe des outils de base
RUN apt update && apt upgrade -y && \
    apt install -y curl nano iputils-ping net-tools && \
    apt install -y x11-apps xeyes && \
    apt clean


# Définit le répertoire de travail
WORKDIR /root

# Commande lancée par défaut : un terminal interactif
CMD ["/bin/bash"]

