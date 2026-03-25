---
layout: default
title: Politique de Confidentialité
lang: fr
other_lang: privacy-policy-en.html
other_lang_label: English
---

# Politique de Confidentialité de FoodNet

**Dernière mise à jour : 25 mars 2026**

## Introduction

FoodNet est une application de gestion alimentaire conçue pour aider à réduire le gaspillage alimentaire en suivant les produits alimentaires et leurs dates de péremption. Cette politique de confidentialité explique comment l'application traite vos données.

## Informations sur le Développeur

FoodNet est développé et maintenu par Alexandre Bailon.

## Collecte et Stockage des Données

### Stockage Local (Par Défaut)

Par défaut, toutes vos données sont stockées localement sur votre smartphone. Cela inclut :
- Les produits alimentaires que vous ajoutez à l'application
- Les dates de péremption
- Les catégories et autres métadonnées

Ces données locales ne quittent jamais votre appareil, sauf si vous choisissez d'utiliser la fonctionnalité de synchronisation.

### Stockage Cloud (Optionnel)

Si vous choisissez de vous connecter à notre service en utilisant l'authentification OAuth :
- Votre adresse e-mail est stockée dans notre base de données Firestore
- Vos données d'inventaire alimentaire peuvent être synchronisées avec Firestore

**Objectif du stockage cloud :**
- Accéder à vos données depuis plusieurs appareils
- Partager votre inventaire alimentaire avec d'autres utilisateurs que vous invitez à votre groupe

## Comment Nous Utilisons Vos Données

Nous utilisons votre adresse e-mail exclusivement pour :
- Authentifier votre compte
- Permettre la synchronisation multi-appareils
- Gérer les adhésions aux groupes pour le partage de données

**Nous ne :**
- Vendons pas vos données à des tiers
- Utilisons pas vos données à des fins publicitaires
- Analysons pas vos données à des fins commerciales
- Partageons pas vos données avec quiconque, sauf les membres du groupe que vous invitez explicitement

## Services Tiers

FoodNet utilise les services tiers suivants :

### Google Firebase
- **Firebase Authentication** : Pour l'authentification sécurisée des utilisateurs via OAuth
- **Cloud Firestore** : Pour le stockage de données cloud optionnel et la synchronisation

Ces services sont soumis à la Politique de Confidentialité de Google : https://policies.google.com/privacy?hl=fr

## Conservation des Données et Disponibilité du Service

Cette application est fournie comme un service gratuit pour aider à réduire le gaspillage alimentaire. Veuillez noter :
- Si les coûts d'hébergement Firestore deviennent trop élevés, le service de synchronisation cloud pourra être interrompu
- Dans ce cas, une solution alternative pourra être proposée, ou l'application pourra revenir à un stockage uniquement local
- Vous serez notifié à l'avance si des modifications du service sont prévues
- Vos données locales resteront toujours accessibles sur votre appareil

## Vos Droits

Vous avez le droit de :
- Utiliser l'application sans vous connecter aux services cloud (mode local uniquement)
- Supprimer votre compte et les données associées à tout moment
- Demander des informations sur les données que nous stockons
- Vous déconnecter du service et supprimer vos données de nos serveurs

## Suppression des Données

Pour supprimer vos données cloud :
1. Ouvrez l'application
2. Déconnectez-vous du service en utilisant l'option de déconnexion dans le menu
3. Vos données seront supprimées de notre base de données Firestore

Les données locales peuvent être supprimées en désinstallant l'application ou en effaçant les données de l'application depuis les paramètres de votre appareil.

## Sécurité

Nous prenons des mesures raisonnables pour protéger vos données :
- Firebase Authentication fournit une connexion sécurisée basée sur OAuth
- La transmission des données est chiffrée en utilisant HTTPS
- L'accès à vos données nécessite une authentification
- Seuls les membres du groupe invités peuvent accéder aux données partagées

## Confidentialité des Enfants

FoodNet ne collecte pas sciemment d'informations provenant d'enfants de moins de 13 ans. L'application est destinée au grand public.

## Modifications de Cette Politique de Confidentialité

Nous pouvons mettre à jour cette politique de confidentialité de temps en temps. Toute modification sera publiée dans ce document avec une date de "Dernière mise à jour" actualisée.

## Nous Contacter

Si vous avez des questions concernant cette politique de confidentialité ou la manière dont vos données sont traitées, veuillez contacter :
- E-mail : contact.gordios@gmail.com
- GitHub : https://github.com/anobli/foodnet/

## Open Source

FoodNet est un logiciel open source sous licence GNU General Public License v2.0 ou ultérieure. Vous pouvez consulter le code source et contribuer sur : https://github.com/anobli/foodnet/

## Consentement

En utilisant FoodNet, vous consentez à cette politique de confidentialité. Si vous choisissez d'utiliser la fonctionnalité de synchronisation cloud, vous consentez au stockage de votre adresse e-mail et de vos données d'inventaire alimentaire dans Firestore comme décrit dans cette politique.
