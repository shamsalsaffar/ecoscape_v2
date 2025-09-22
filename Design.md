### Vilken del/feature har ni arbetat med? (Refaktorering eller
ny).

Vi vill refaktorera vår booking service och göra den mer modulär. Vi vill även implementera
realtime notifikationer för user och host som kopplas med vår booking service. Det gör så att
en användare kommer se notifikationer baserat på om dem har bokat, avbokat eller fått sin
bokning cancelled. Vi ska separera logiken i vår booking service för att minska duplicering och bryta ner vår
god method.

### Vad ingår och vad ingår inte?

Notifikationerna kommer att skicka i sambands med events som tidigare nämnts. Men se till att skaffa en bra grund så
att notifikationer kan skickas som olika typer i.e för alla users och inte bara en user. Vi kommer bara fokusera på dessa interna
notifikationerna dvs så man kan få en notifikation eller inte men vi kommer inte se till att man får en extern notifikation
och endast fokusera på det interna. Vår email service kommer att existera och jobba parallelt så vi notifierar inte på samma sätt
som notifikationer men vi kommer skapa en bra grund att göra det till framtiden. Så vi kommer inte ha email template eller events.

### Refaktorering: beskriv vilka problem ni såg t.ex. svagheter i designen, tecken på dålig struktur, brister i ansvarsfördelning eller beroenden m.m. Ge exempel med antingen kodexempel (printscreen) eller enkel förklaring.

Vi har för mycket ansvar i booking service vilket gör att vi har en god method som gör 7 olika grejer. Vi ville skapa en bättre struktur, dela upp på ansvaret och
minska duplicering för att få en ren kod.

### Ny funktionalitet: beskriv behovet/problemet, vilket use case eller krav saknades?

Vi saknade en notifiering vilket kan skapa förvirring mellan användare, så får att fixa det problemet så ville vi implementera in-app realtime notifikationer för de use cases vi tyckte var viktigaste att ha notifikationer för så som cancel, create och update booking. Så för att höja användarupplevelsen och höja projektkvaliten så trots om vi skickar email konfirmation så hade pushat notifikationer ökat användarupplevelsen mer vilket är något vi strävar efter.

### Designval (principer/mönster)

Vi har valt att följa

Principer: SRP, KISS, DRY, OPEN CLOSE
Mönster: TEMPLATE, STRATEGY, DATA MAPPER, REPOSITORY

### Motivera varför just dessa passar för ert problem.

Eftersom vi tidigare skrivit allt i booking service så valde vi att följa SRP för att dela upp på logiken. Men generellt så vill vi följa KISS och DRY i vår kod både backend och frontend då vi vill ha en hög kvalité på vår kod.

När vi kommer till mönster så gör template att vi undviker duplicering utav kod i vår notifikation bygglogik genom att centrilisera gemensam struktur medans specifika implementationer kan variera. Vi har en abstrakt notification template som har tre abstrakta metoder dom bygger header, body och footer. Child klasserna är BookingCancellationNotificationTemplate, BookingCreationNotificationTemplate, BookingDetailsUpdateNotificationTemplate dom extendar notificationtemplate men implementerar det på varsit sätt avseende notificationType. Detta gör att vi undviker skriva om kod och om vi vill i framtiden implementera fler notifikationstyper så använder vi oss en satt grundstruktur.

<img width="1338" height="774" alt="image" src="https://github.com/user-attachments/assets/280b412d-7dd1-46e4-9ba0-cf7e8d2b5331" />

Vi har har även skapat en till Template klass som är notificationService som hantera olika kanaler. Just nu är det bara pushnotifications som extendar notificationService och implementerar de två abstraktametoder, sentToUser och broadcast. Men i framtiden hade velat lägga till email och sms som då hade implementerat sendToUser och broadcast på sina egna sätt.

<img width="972" height="358" alt="image" src="https://github.com/user-attachments/assets/cf5f4c74-83a4-412c-8a2c-8ee53eb15f2f" />


En Validation Strategy är ett designmönster som gör att valideringslogiken kan bytas ut beroende på bokningskontext utan att man behöver ändra den centrala logiken i BookingService.
Istället för långa if/else-kedjor väljer systemet en policy (strategi) som bestämmer vilka regler som ska köras.

### Beskriv den nya designen: hur ser ansvars- och rollfördelningen ut?

Vi har en notifikationtemplate som är en abstraktklass och den har som har tre abstrakta metoder och den abstraktaklassen i sin tur har tre childklasser (bookingCancellationNotificationTemplate, BookingCreationNotificationTemplate, BookingUpdateNotificationTemplate)

BookingService -> väljer policy -> Validation Strategy bygger pipeline -> Pipeline kör regler (Date, Guest, Contact, Availability) -> Vid fel: kastar BusinessValidationException -> Vid succé: flödet fortsätter

### Förklara hur principer/mönster har implementerats.

Genom att vi har skapat en parent klass med tre childklasser som implementerar abstrakta metoder ifrån parent klassen så har vi då använt oss utav Template Pattern.

Strategy = välja rätt policy för validering beroende på kontext.

Pipeline = köra de valda reglerna i sekvens.

Resultatet blir renare kod, mer flexibel logik, lättare testning och tydligare ansvarsfördelning.

<img width="1700" height="1424" alt="image" src="https://github.com/user-attachments/assets/0378355f-7287-419b-91cb-1574dc8c2a65" />
