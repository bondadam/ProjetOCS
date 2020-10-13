# Projet OCS - Groupe 3

- ##### *Munier Rémy*
- ##### *Bond Adam*
- ##### *Ala Younes*

## Sac à dos connecté

## Description

##### Features:

**Deux familles de features:**

1. Les aides à la route et features ergonomiques

Le sac détecte les conditions de lumière basse et allume des lumières internes quand le sac est ouvert pour aider l'utilisateur à trouver ses affaires. Une lumière externe permet aussi à l'utilisateur de s'orienter en cas d'urgence, activable grâce à un bouton.

Un accéléromètre est présent sur le sac et permet aux feux de freins (LEDs sur le dos du sac) de s'allumer automatiquement quand le cycliste baisse brusquement de vitesse. Le capteur sert aussi à détecter un choc, dans ce cas une notification est envoyée à l'utilisateur, si il le réponds pas, une alerte d'urgence peut être envoyées a des (mails/numéro de tel/utilisateurs ?) enregistrés dans l'appli, de plus les LEDs du sac clignotent pour émettre un SOS lumineux.

Un capteur de distance est présent à l'arrière pour indiquer à l'utilisateur quand un véhicule le suit de trop près. Cette alerte ne se fait pas à l'arrêt mais uniquement pendant le déplacement (détecté grâce à l'accéléromètre).

2. Le relais au téléphone

Le sac sert de relais au téléphone portable du cycliste en lui permettant de se tenir au courant de ses notifications (appels, messages, etc) de manière sûre avec des vibrations sur chaque épaulière. Le relais va dans les deux sens puisque le sac à dos dispose d'un système de cordons sur lequel l'utilisateur peut tirer pour indiquer une décision.

La hiérarchisation entre notifications a pour but de permettre à l'utilisateur de savoir quel type de notification l'attend en fonction des caractéristiques (force, temps, répétition) de la vibration et de prendre une décision (arrêt ou non) en fonction. Si deux notifications ont lieu en même temps, cela permet aussi de savoir laquelle doit prendre la priorité quand on informe l'utilisateur (Appels > Messages > Misc).

Ici nous envisageons aussi à terme de rendre une API libre pour programmer les lumières et les cordons, ce qui permettrait à d'autres utilisateurs de créer des utilisations correspondant à d'autres coeurs de métiers.

3. Un scénario d'utilisation se servant des deux types de features:

L'utilisateur choisit un parcours sur son téléphone. A chaque fois qu'il arrive à une intersection, les vibrations sur l'épaulière lui indiqueront la direction dans laquelle il veut tourner. Les épaulières vibrent différamment pour indiquer une notification. Le cycliste s'arrête pour lire son message, décide de faire un détours en conséquence, et tire sur un cordon pour allumer la lumière correspondante à l'arrière car il veut prendre une direction contraire à celle indiquée par son GPS.


##### Services :
  
## Matériel

##### Capteurs

- Capteur de distance à l'arrière
- Accéléromètre pour les feux de frein et la détection de choc
- Capteur de luminosité
- Capteur de force/pression pour le système de cordes

##### Actionneurs

- LEDs
- Vibrations