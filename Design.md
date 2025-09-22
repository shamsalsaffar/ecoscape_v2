### Vilken del/feature har ni arbetat med? (Refaktorering eller ny).

Vi vill refaktorera vår booking service och göra den mer modulärch lättare att underhålla. Tidigare låg mycket logik samlad i samma klass och metoderna blev både långa och duplicerade. Vi vill även implementera
realtime notifikationer för user och host som kopplas med vår booking service. Det gör så att
en användare kommer se notifikationer baserat på om dem har bokat, avbokat eller fått sin
bokning cancelled. 

### Vad ingår och vad ingår inte?

Notifikationerna kommer att skicka i sambands med events som tidigare nämnts. Men se till att skaffa en bra grund så
att notifikationer kan skickas som olika typer i.e för alla users och inte bara en user. Vi kommer bara fokusera på dessa interna
notifikationerna dvs så man kan få en notifikation eller inte men vi kommer inte se till att man får en extern notifikation
och endast fokusera på det interna. Vår email service kommer att existera och jobba parallelt så vi notifierar inte på samma sätt
som notifikationer men vi kommer skapa en bra grund att göra det till framtiden. Så vi kommer inte ha email template eller events.

### Refaktorering: beskriv vilka problem ni såg t.ex. svagheter i designen, tecken på dålig struktur, brister i ansvarsfördelning eller beroenden m.m. Ge exempel med antingen kodexempel (printscreen) eller enkel förklaring.

BookingService hade för mycket ansvar samlat i en och samma klass. Det mest tydliga problemet var att vi hade en “god metod” som gjorde upp till sju olika saker samtidigt: validerade data, hanterade kalendern, satte priser, skickade mail, uppdaterade status, sparade till databasen och returnerade ett svar.

Detta gav flera svagheter i designen:

- Bristande ansvarsfördelning (SRP) → en metod skötte flera olika logiker.

- Dålig struktur → koden blev svår att läsa och underhålla.

- Duplicering → samma valideringslogik förekom på flera ställen.

- Starka beroenden → BookingService kände till för många detaljer om andra delar
  
### Ny funktionalitet: beskriv behovet/problemet, vilket use case eller krav saknades?

Vi saknade en notifiering vilket kan skapa förvirring mellan användare, så får att fixa det problemet så ville vi implementera in-app realtime notifikationer för de use cases vi tyckte var viktigaste att ha notifikationer för så som cancel, create och update booking. Så för att höja användarupplevelsen och höja projektkvaliten så trots om vi skickar email konfirmation så hade pushat notifikationer ökat användarupplevelsen mer vilket är något vi strävar efter.

### Designval (principer/mönster)

Vi har valt att följa

Principer: SRP, KISS, DRY, OPEN CLOSE
Mönster: TEMPLATE, STRATEGY, DATA MAPPER, REPOSITORY

### Motivera varför just dessa passar för ert problem.

Eftersom vi tidigare hade samlat all logik i BookingService valde vi att följa Single Responsibility Principle (SRP) för att bryta ner koden i mindre delar med tydligt ansvar. Det gör att varje klass eller komponent har ett specifikt syfte, vilket både ökar läsbarheten och underlättar underhåll.
Vi har också följt KISS (Keep It Simple, Stupid) och DRY (Don’t Repeat Yourself) för att undvika onödig komplexitet och duplicerad kod.
vi använda Validation Strategy (genom BookingValidationPipeline och de olika BookingValidator-klasserna som Contact-, Date- och GuestValidator). Istället för långa if/else-block i BookingService använder vi en strategi där varje valideringsregel är en egen klass.

När vi kommer till mönster så gör template att vi undviker duplicering utav kod i vår notifikation bygglogik genom att centrilisera gemensam struktur medans specifika implementationer kan variera. Vi har en abstrakt notification template som har tre abstrakta metoder dom bygger header, body och footer. Child klasserna är BookingCancellationNotificationTemplate, BookingCreationNotificationTemplate, BookingDetailsUpdateNotificationTemplate dom extendar notificationtemplate men implementerar det på varsit sätt avseende notificationType. Detta gör att vi undviker skriva om kod och om vi vill i framtiden implementera fler notifikationstyper så använder vi oss en satt grundstruktur.

<img width="1338" height="774" alt="image" src="https://github.com/user-attachments/assets/280b412d-7dd1-46e4-9ba0-cf7e8d2b5331" />

Vi har har även skapat en till Template klass som är notificationService som hantera olika kanaler. Just nu är det bara pushnotifications som extendar notificationService och implementerar de två abstraktametoder, sentToUser och broadcast. Men i framtiden hade velat lägga till email och sms som då hade implementerat sendToUser och broadcast på sina egna sätt.

<img width="972" height="358" alt="image" src="https://github.com/user-attachments/assets/cf5f4c74-83a4-412c-8a2c-8ee53eb15f2f" />



### Beskriv den nya designen: hur ser ansvars- och rollfördelningen ut?

Vi har en notifikationtemplate som är en abstraktklass och den har som har tre abstrakta metoder och den abstraktaklassen i sin tur har tre childklasser (bookingCancellationNotificationTemplate, BookingCreationNotificationTemplate, BookingUpdateNotificationTemplate)

### Booking – ansvar och roller (refaktoreringen):
- **BookingService (orkestrering):** Minimal flödeslogik. Hämtar user/listing, triggar validering, delegerar kalender- och prisberäkning, sparar och mappar svar.
- **BookingValidationPipeline (Strategy + Pipeline):** Bygger kedjan av regler och kör dem i turordning.

1. **DateValidator** – datumrelationer (ej dåtid, slut efter start).
2. **GuestValidator** – min/max och mot listingens capacity.
3. **ContactValidator** – namn/telefon/e-postformat.
4. **Availability** – kontrollerar lediga datum via ListingAvailableDatesService.

- **CalendarOrchestrator:** tryRescheduleOrThrow, reschedule, release – isolerar kalenderlogiken.
- **PriceService:** Beräknar price per night, cleaningFee, serviceFee och total (återanvänds vid create/update).
- **BookingMapper (MapStruct):** Entity ⇄ DTO, inkl. AfterMapping för websiteFee.
- **EffectiveBookingRequestFactory:** Bygger “effektivt” uppdateringsobjekt (kombinerar inkommande fält med befintliga).

#### Systemflöde :
BookingService -> väljer policy -> Validation Strategy bygger pipeline -> Pipeline kör regler (Date, Guest, Contact, Availability) -> Vid fel: kastar BusinessValidationException -> Vid succé: flödet fortsätter
### Förklara hur principer/mönster har implementerats.

Genom att vi har skapat en parent klass med tre childklasser som implementerar abstrakta metoder ifrån parent klassen så har vi då använt oss utav Template Pattern.

#### Booking – principer & Patterns: 
- **SRP/KISS/DRY:** Varje klass har ett tydligt ansvar; enkel, icke-duplicerad logik.
- **Strategy + Pipeline (Validation):** Regler bryts ut i separata validatorer som kan kombineras/ändras utan att röra BookingService.
- **Mapper Pattern (BookingMapper/AfterMapping som “hook” för prissättning):** Prisdelar injiceras efter mapping utan att blanda DTO-logik med beräkningar.
- **Factory (EffectiveBookingRequestFactory):** Standardiserar uppdateringar — minskar if/else-spagetti och sidEffekter.
- **Orchestrator (CalendarOrchestrator):** Inkapslar schemaläggning/återställning av datum så att BookingService förblir tunn.

### Konkreta effekter:
- Renare controller/service-gräns (mindre kod i BookingService).
- Testbarhet (enhetstester per validator, PriceService, Orchestrator och Mapper).
- Utbyggbarhet (lägg till en ny regel → ny validator; ny prislogik → PriceService; ny kalenderpolicy → Orchestrator). 

### Klassdiagram för valideringssystemet efter refaktorering

<img width="1700" height="1424" alt="image" src="https://github.com/user-attachments/assets/0378355f-7287-419b-91cb-1574dc8c2a65" />
