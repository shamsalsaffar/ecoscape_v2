# Introduktion 

Systemet ska skicka automatiserad notifikationer med samband med bokning eller avbokning. Push notifikationer ska skickas till både USER och HOST. Notifikationerna ska skickas i realtid och säkerställa att lagra notifikationerna om en USER är offline just då. Vi ska använda oss utav template mönster. 

Vi ska även refaktorera vårt system för att designa om vårt boknings system genom att bryta ner en metod för att följa principen SRP och strategy mönster.

# Intressenter

Grupp A Utvecklingsteam (Iasmina & Alexander och Shams)

Grupp B Slutanvändare (Iasmina)

Lärare Produktägare

# Kravspecifikation

## Funktionella krav

### 3.1.1 Automatiska notiser

FK-001. Vid en ny bokning ska en bekräftelsenotis skickas till en USER och HOST.

FK-002. Vid avbokning ska en avbokningsnotis skickas.

FK-003. Lagra notifikationerna i databasen.

### 3.1.2 Bokningshantering

FK-004. En USER ska kunna boka en listing.

FK-005. En USER ska kunna avboka en listing.


## Icke-funktionella krav

### 3.2.1 Säkerhet

IFK-001. Endast autentisierade USERS ska kunna prenumerera på sina egna kanaler.

IFK-002. Endast autentisierade USERS och HOSTS ska kunna ta emot notiser.

### 3.2.2 Prestanda

IFK-003. Systemet ska leverera en push-notis inom 5 sekunder när en bokning och avbokning har hänt.

IFK-004. Systemet ska uppdateras i realtid för online användare.

### 3.2.3 Lagring

IFK-005. En notifikation ska bevaras i minst 90 dagar i databasen.

### 3.2.4 Underhållbarhet

IFK-006. Notifieringslogiken ska vara modulär och följa Template Method Pattern.

IFK-007. Nya notistyper ska kunna läggas till utan att ändra befintliga klasser.

# Prioriteringar & beroenden

Krav-ID - Prioritet - Beroenden

FK-001 - Must have - FK-004

FK-002 - Must have - FK-005

FK-003 - Must have - FK-001

FK-004 - Must have -

FK-005 - Must have

IFK-001 - Must have - 

IFK-002 - Must have -

IFK-003 - Must have - FK-001

IFK-004 - Must have - FK-001

IFK-005 - Must have -

IFK-006 - Must have -

IFK-007 - Must have -

POSTMAN DOCUMENTATION:

https://documenter.getpostman.com/view/40897736/2sAYk7SjT2
