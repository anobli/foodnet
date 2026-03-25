---
layout: default
title: Privacy Policy
lang: en
other_lang: privacy-policy-fr.html
other_lang_label: Français
---

# Privacy Policy for FoodNet

**Last updated: March 25, 2026**

## Introduction

FoodNet is a food management application designed to help reduce food waste by tracking food items and their expiration dates. This privacy policy explains how the app handles your data.

## Developer Information

FoodNet is developed and maintained by Alexandre Bailon.

## Data Collection and Storage

### Local Storage (Default)

By default, all your data is stored locally on your smartphone. This includes:
- Food items you add to the app
- Expiration dates
- Categories and other metadata

This local data never leaves your device unless you choose to use the synchronization feature.

### Cloud Storage (Optional)

If you choose to connect to our service using OAuth authentication:
- Your email address is stored in our Firestore database
- Your food inventory data may be synchronized to Firestore

**Purpose of cloud storage:**
- Access your data from multiple devices
- Share your food inventory with other users you invite to your group

## How We Use Your Data

We use your email address exclusively for:
- Authenticating your account
- Enabling multi-device synchronization
- Managing group memberships for data sharing

**We do not:**
- Sell your data to third parties
- Use your data for advertising purposes
- Analyze your data for any commercial purpose
- Share your data with anyone except the group members you explicitly invite

## Third-Party Services

FoodNet uses the following third-party services:

### Google Firebase
- **Firebase Authentication**: For secure user authentication via OAuth
- **Cloud Firestore**: For optional cloud data storage and synchronization

These services are subject to Google's Privacy Policy: https://policies.google.com/privacy

## Data Retention and Service Availability

This app is provided as a free service to help reduce food waste. Please note:
- If Firestore hosting costs become too expensive, the cloud synchronization service may be discontinued
- In such case, an alternative solution may be provided, or the app may revert to local-only storage
- You will be notified in advance if any changes to the service are planned
- Your local data will always remain accessible on your device

## Your Rights

You have the right to:
- Use the app without connecting to cloud services (local-only mode)
- Delete your account and associated data at any time
- Request information about the data we store
- Disconnect from the service and remove your data from our servers

## Data Deletion

To delete your cloud data:
1. Open the app
2. Disconnect from the service using the disconnect option in the menu
3. Your data will be removed from our Firestore database

Local data can be deleted by uninstalling the app or clearing the app's data from your device settings.

## Security

We take reasonable measures to protect your data:
- Firebase Authentication provides secure OAuth-based login
- Data transmission is encrypted using HTTPS
- Access to your data requires authentication
- Only invited group members can access shared data

## Children's Privacy

FoodNet does not knowingly collect information from children under 13 years of age. The app is intended for general audiences.

## Changes to This Privacy Policy

We may update this privacy policy from time to time. Any changes will be posted in this document with an updated "Last updated" date.

## Contact Us

If you have questions about this privacy policy or how your data is handled, please contact:
- Email: contact.gordios@gmail.com
- GitHub: https://github.com/anobli/foodnet/

## Open Source

FoodNet is open source software licensed under the GNU General Public License v2.0 or later. You can view the source code and contribute at: https://github.com/anobli/foodnet/

## Consent

By using FoodNet, you consent to this privacy policy. If you choose to use the cloud synchronization feature, you consent to the storage of your email address and food inventory data in Firestore as described in this policy.
