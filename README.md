# Projet OCS - Groupe 3

- ##### *Munier Rémy*
- ##### *Bond Adam*
- ##### *Ala Younes*

## Sac à dos connecté

## Description

##### Features:

- GPS qui permet de choisir un parcours et qui allume les leds du sac en fonction pour communiquer avec les autres usagers de la route les intentions du porteur du sac. Les instructions sont communiquées au porteur à l'aide de vibrations. Les feux de freins s'activent automatiquement grâce à un accéléromètre.

- L'extérieur et l'intérieur du sac sont éclairés lorsqu'il fait sombre, pour rendre plus visible le porteur du sac et l'aider à trouver ses affaires.

- Localisation du sac à l'aide d'un GPS en cas de perte.

- Le sac détecte les accidents potentiels à l'aide d'un accéléromètre. Si un accident est détecté, le sac envoie une notification (téléphone? vibrations?). Si la notification n'a pas de réponse dans un temps déterminé, un signal d'urgence est envoyé avec la position, la vitesse, etc, et les leds clignotent pour faire un signal SOS.

- Les sacs sont reliés à une base de données globale. A chaque fois que l'utilisateur a fini un trajet, il peut noter le parcours comme "accessible" ou non. De plus, on analyse la vitesse du trajet, les arrêts etc pour donner une note d'accessibilité générée automatiquement si l'utilisateur ne note pas. Le sac prend en compte ces données dans le choix d'un parcours.


- Dashcam ?


##### Services :
  - Géolocalisation Live du sac
  - Signalement de la position GPS en cas de choc
  - Trajet enregistrables et direction avec vibrations+clignotants ( ou est-ce que c'est de base dans le sac et pas un service ?)

## Matériel

##### Capteurs :
- Capteurs de distance : 3 ?
- Accéléromètre ( active les leds de freins)
- Capteur de choc (nom ?)
- GPS

##### Actionneurs :
- LEDs
- Vibrations