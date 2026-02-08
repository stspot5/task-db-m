CREATE TABLE klient (
    klient_id NUMBER PRIMARY KEY,
    ime       VARCHAR2(50) NOT NULL,
    familia   VARCHAR2(50) NOT NULL,
    adres     VARCHAR2(200),
    telefon   VARCHAR2(30)
);


CREATE TABLE dogovor (
    dogovor_id   NUMBER PRIMARY KEY,
    klient_id    NUMBER NOT NULL,
    tip          VARCHAR2(20) NOT NULL,  -- 'REKLAMA' или 'RABOTA'
    nachalna_data DATE NOT NULL,
    kraina_data   DATE,
    opisanie      VARCHAR2(400),

    CONSTRAINT fk_dogovor_klient
        FOREIGN KEY (klient_id) REFERENCES klient(klient_id),

    CONSTRAINT chk_dogovor_tip
        CHECK (tip IN ('REKLAMA','RABOTA'))
);


CREATE TABLE plashtane (
    plashtane_id   NUMBER PRIMARY KEY,
    dogovor_id     NUMBER NOT NULL,
    data_na_plashtane DATE NOT NULL,
    opisanie       VARCHAR2(200),

    CONSTRAINT fk_plashtane_dogovor
        FOREIGN KEY (dogovor_id) REFERENCES dogovor(dogovor_id)
);

CREATE TABLE faktura (
    faktura_id      NUMBER PRIMARY KEY,
    plashtane_id    NUMBER NOT NULL,
    cena            NUMBER(10,2) NOT NULL,
    data_izdavane   DATE NOT NULL,
    firma_izpulnitel VARCHAR2(100),
    firma_poruchitel VARCHAR2(100),
    status          VARCHAR2(20) NOT NULL,

    CONSTRAINT fk_faktura_plashtane
        FOREIGN KEY (plashtane_id) REFERENCES plashtane(plashtane_id),

    CONSTRAINT chk_faktura_status
        CHECK (status IN ('PAID','UNPAID','CANCELLED'))
);

CREATE TABLE otdel (
    otdel_id NUMBER PRIMARY KEY,
    ime      VARCHAR2(80) NOT NULL
);

CREATE TABLE rola (
    rola_id NUMBER PRIMARY KEY,
    ime     VARCHAR2(80) NOT NULL
);

CREATE TABLE sluzhitel (
    sluzhitel_id NUMBER PRIMARY KEY,
    otdel_id     NUMBER NOT NULL,
    rola_id      NUMBER NOT NULL,

    ime          VARCHAR2(50) NOT NULL,
    familia      VARCHAR2(50) NOT NULL,
    adres        VARCHAR2(200),
    telefon      VARCHAR2(30),
    el_poshta    VARCHAR2(120),
    data_naznachavane DATE NOT NULL,

    tip          VARCHAR2(20) NOT NULL,  -- 'ADMIN','ACCOUNTANT','DESIGNER'
    nivo         NUMBER DEFAULT 1,

    CONSTRAINT fk_sluzhitel_otdel
        FOREIGN KEY (otdel_id) REFERENCES otdel(otdel_id),

    CONSTRAINT fk_sluzhitel_rola
        FOREIGN KEY (rola_id) REFERENCES rola(rola_id),

    CONSTRAINT chk_sluzhitel_tip
        CHECK (tip IN ('ADMIN','ACCOUNTANT','DESIGNER'))
);

CREATE TABLE zadacha (
    zadacha_id NUMBER PRIMARY KEY,
    sluzhitel_id NUMBER NOT NULL,
    ime         VARCHAR2(100) NOT NULL,
    opisanie    VARCHAR2(400),

    CONSTRAINT fk_zadacha_sluzhitel
        FOREIGN KEY (sluzhitel_id) REFERENCES sluzhitel(sluzhitel_id)
);

CREATE TABLE obratna_vruzka (
    obratna_vruzka_id NUMBER PRIMARY KEY,
    klient_id NUMBER NOT NULL,
    ime       VARCHAR2(100),
    suzdatel  VARCHAR2(100),
    komentar  VARCHAR2(400),
    reiting   NUMBER(2),

    CONSTRAINT fk_obratna_klient
        FOREIGN KEY (klient_id) REFERENCES klient(klient_id),

    CONSTRAINT chk_reiting
        CHECK (reiting BETWEEN 1 AND 5)
);

CREATE TABLE reklami (
    reklama_id NUMBER PRIMARY KEY,
    dogovor_id NUMBER NOT NULL UNIQUE,

    ime        VARCHAR2(120) NOT NULL,
    tip        VARCHAR2(50),
    data_startirane DATE NOT NULL,
    data_priklyuchvane DATE,

    CONSTRAINT fk_reklama_dogovor
        FOREIGN KEY (dogovor_id) REFERENCES dogovor(dogovor_id)
);

CREATE TABLE reklama_atributi (
    reklama_id NUMBER PRIMARY KEY,
    klikaniya  NUMBER DEFAULT 0,
    impresii   NUMBER DEFAULT 0,

    CONSTRAINT fk_attr_reklama
        FOREIGN KEY (reklama_id) REFERENCES reklami(reklama_id)
);


-- Отдели
INSERT INTO otdel VALUES (1, 'Продажби');
INSERT INTO otdel VALUES (2, 'Дизайн');
INSERT INTO otdel VALUES (3, 'Финанси');

-- Роли
INSERT INTO rola VALUES (1, 'Мениджър');
INSERT INTO rola VALUES (2, 'Специалист');
INSERT INTO rola VALUES (3, 'Стажант');

-- Служители
INSERT INTO sluzhitel VALUES (1, 1, 1, 'Иван', 'Петров', 'Русе', '1111123456', 'ivan@firma.bg', DATE '2023-01-10', 'ADMIN', 3);
INSERT INTO sluzhitel VALUES (2, 2, 2, 'Мария', 'Иванова', 'Русе', '2222234567', 'maria@firma.bg', DATE '2024-03-05', 'DESIGNER', 2);
INSERT INTO sluzhitel VALUES (3, 3, 2, 'Георги', 'Стоянов', 'Русе', '0333345678', 'georgi@firma.bg', DATE '2022-09-20', 'ACCOUNTANT', 4);
INSERT INTO sluzhitel VALUES (4, 2, 3, 'Елена', 'Тодорова', 'Русе', '0444456789', 'elena@firma.bg', DATE '2025-01-15', 'DESIGNER', 1);

-- Клиенти
INSERT INTO klient VALUES (1, 'Петър', 'Колев', 'Русе, Център', '1212000001');
INSERT INTO klient VALUES (2, 'Николай', 'Димитров', 'София', '1313000002');
INSERT INTO klient VALUES (3, 'Анна', 'Георгиева', 'Варна', '1414000003');
INSERT INTO klient VALUES (4, 'Стефан', 'Михайлов', 'Пловдив', '1515000004');

-- Договори
INSERT INTO dogovor VALUES (1, 1, 'REKLAMA', DATE '2025-01-01', DATE '2025-02-01', 'Реклама във Facebook');
INSERT INTO dogovor VALUES (2, 1, 'RABOTA',  DATE '2025-02-10', DATE '2025-03-01', 'Дизайн на лого');
INSERT INTO dogovor VALUES (3, 2, 'REKLAMA', DATE '2025-03-01', DATE '2025-04-01', 'Google Ads кампания');
INSERT INTO dogovor VALUES (4, 3, 'RABOTA',  DATE '2025-01-15', DATE '2025-01-30', 'Банери за сайт');
INSERT INTO dogovor VALUES (5, 4, 'REKLAMA', DATE '2025-02-01', NULL, 'Дългосрочна реклама');

-- Плащания
INSERT INTO plashtane VALUES (1, 1, DATE '2025-01-05', 'Първо плащане');
INSERT INTO plashtane VALUES (2, 1, DATE '2025-01-20', 'Второ плащане');
INSERT INTO plashtane VALUES (3, 2, DATE '2025-02-15', 'Плащане за лого');
INSERT INTO plashtane VALUES (4, 3, DATE '2025-03-05', 'Плащане за Ads');
INSERT INTO plashtane VALUES (5, 4, DATE '2025-01-20', 'Плащане за банери');

-- Фактури
INSERT INTO faktura VALUES (1, 1, 250.00, DATE '2025-01-05', 'Media Agency', 'Client A', 'PAID');
INSERT INTO faktura VALUES (2, 2, 150.00, DATE '2025-01-20', 'Media Agency', 'Client A', 'UNPAID');
INSERT INTO faktura VALUES (3, 3, 500.00, DATE '2025-02-15', 'Design Studio', 'Client A', 'PAID');
INSERT INTO faktura VALUES (4, 4, 800.00, DATE '2025-03-05', 'Ads Agency', 'Client B', 'PAID');
INSERT INTO faktura VALUES (5, 5, 300.00, DATE '2025-01-20', 'Design Studio', 'Client C', 'CANCELLED');

-- Реклами
INSERT INTO reklami VALUES (1, 1, 'FB Кампания - Клиент 1', 'Facebook', DATE '2025-01-01', DATE '2025-02-01');
INSERT INTO reklami VALUES (2, 3, 'Google Ads - Клиент 2', 'Google', DATE '2025-03-01', DATE '2025-04-01');
INSERT INTO reklami VALUES (3, 5, 'Дългосрочна реклама - Клиент 4', 'Facebook', DATE '2025-02-01', NULL);

-- Рекламни атрибути
INSERT INTO reklama_atributi VALUES (1, 120, 15000);
INSERT INTO reklama_atributi VALUES (2, 90, 12000);
INSERT INTO reklama_atributi VALUES (3, 300, 50000);

-- Задачи
INSERT INTO zadacha VALUES (1, 2, 'Банери', 'Изработка на банери за реклама');
INSERT INTO zadacha VALUES (2, 2, 'Лого', 'Дизайн на лого за клиент');
INSERT INTO zadacha VALUES (3, 4, 'Рекламен пост', 'Пост за социални мрежи');
INSERT INTO zadacha VALUES (4, 3, 'Фактуриране', 'Издаване на фактури');

-- Обратна връзка
INSERT INTO obratna_vruzka VALUES (1, 1, 'Петър', 'Клиент', 'Много добро обслужване', 5);
INSERT INTO obratna_vruzka VALUES (2, 2, 'Николай', 'Клиент', 'ОК, но може по-бързо', 4);
INSERT INTO obratna_vruzka VALUES (3, 1, 'Петър', 'Клиент', 'Закъсня с 2 дни', 3);

COMMIT;
