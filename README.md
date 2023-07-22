# Justjoin.it Parser

## Opis
Moja aplikacja umożliwia skrejpowanie i parsowanie ofert dostępnych na portalu justjoin.it. 
W zależności od podanych parametrów, aplikacja jest w stanie zwrócić listę najbardziej pożądanych umiejętności,
które są aktualnie najczęściej wymieniane w ofertach pracy, albo zwrócić zestaw informacji z samej oferty.


## Technologie
Aplikacja została stworzona przy użyciu następujących technologii:

- Spring Boot 2.7.3
- Webflux
- RabbitMQ
- MongoDB
- Docker
- Selenium
- REST
- Swagger

## Uruchomienie aplikacji
1) Aplikacja i jej składowe są skonteneryzowane, i mogą być uruchomione poleceniem:

    `$ docker-compose -f docker-compose-with-app.yml up -d`

Upewnij się że masz wolne porty:
- 8081 (mongo-express)
- 8083 (aplikacja)
- 27017 (mongodb)
- 4444 (selenium)
- 5672 (rabbitmq)
- 15672 (rabbitmq-management)

Lub przekieruj porty w pliku **docker-compose-with-app.yml**

Po uruchomieniu aplikacji, pod endpointem http://localhost:8083/webjars/swagger-ui/index.html dostępny będzie Swagger, opisujący endpointy.

2) Inny sposób uruchomienia - lokalnie, otwierając aplikacja w IDE, poprzednio wpisując polecenie `$ docker-compose up -d`, które podniesie niezbędne 
skłądowe aplikacji, takie jak MongoDB, RabbitMQ, Selenium.

## Funkcjonalności
Główną funkcjonalnością aplikacji jest skrejpowanie i parsowanie ofert pracy z portalu justjoin.it.

Dzięki temu użytkownik ma możliwość: 
1) Podania konkretnych parametrów i otrzymania na podstawie nich listy najczęściej pożądanych umiejętności.
2) Otrzymanie zestawu danych do każdej oferty, pasującej podanym parametrom.

## Opis techniczny
Reaktywna aplikacja, napisana w Webflux, łącząca w sobie skrejpowanie ofert za pomocą Selenium, przetworzenie i rektywne zapisanie 
danych do MongoDB oraz wysyłka na kolejki RabbitMQ.

Przy starcie aplikacji, uruchamia się scheduler inicjujący, który parsuje oferty dla podanych parametrów, które są umieszczone
w pliku application.yaml (scheduler.init-parse-offers).

Aplikacja opiera się o 3 rodzaje parametrów: **miasto** oferty, **technologia** oraz **poziom stanowiska**.

Aplikacja ma wystawione endpointy:

**POST** ``/offer/actual`` -- aplikacja parsuje oferty dla podanych parametrów, i zwraca wyniki, w pastaci reaktywnego streamu (text/event-stream).

*Najlepiej testować w Postman v.10.10, lub wyższej, ze względu na wsparcie server-sent event (https://blog.postman.com/support-for-server-sent-events/)*

**GET** ``/offer/skill/existing/top/`` -- aplikacja zwraca top X umięjętności, które są najczęściej spotykane w ofertach (zgodnie z podanymi parametrami ofert)

Więcej szczegółow zawiera Swagger - http://localhost:8083/webjars/swagger-ui/index.html

Aplikacja również ma scheduler, ustawiony na 06:00 AM, który uruchamia parsowanie wszystkich offert (które obsługuje aplikacja) oraz wysyłanie wyników na odpowiednie kolejki, zgodne z parametrami ofert.

## Wnioski
Aplikacja, to doskonałe narzędzie dla każdego, kto chce dowiedzieć się, jakie umiejętności są obecnie najbardziej pożądane na rynku pracy w branży IT. Aplikacja jest prosta w obsłudze, szybka i niezawodna.

## Wersja
Aktualna wersja aplikacji to 1.0.0-SNAPSHOT.

## Autor
[https://github.com/maksym-severyn]
