Chat
====
* Written By Bashlykov Sergey
* Copyright (c) 2014 Bashlykov Sergey
* All rights reserved. 
* Console chat - multithreaded server and client written in Java, config and users stored in XML
* 
Консольный чат.
Мультипоточный сервер который принимает сообщения от одного клиента и рассылает их всем подключенным на данный момент пользователям. 
При подключении нового пользователя, сервер отсылает ему N последних сообщений от других пользоватлей подключенных к серверу. 
При получении сообщений от пользователей, сервер так же фиксирует сопроводительную информацию, такую как дата и время сообщения, имя пользователя, его IP - адрес и режим (sleep, eat, work, online).
Настройки сервера, такие как порт, N последних сообщений, максимальное число подключаемых пользователей храняться и считываются посредством XML файла.
Клиенты при подключении получают сгенерированое имя, которое можно сменить и зарегистрировать на сервере. Если имя зарегистрировано, то при смене имени нужно ввести пароль. Данные о пользователях так же хранятся в XML фйле.
