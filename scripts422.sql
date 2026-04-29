-- Таблица "Человек"
CREATE TABLE person (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        age INT,
                        has_driving_license BOOLEAN DEFAULT FALSE,
                        car_id INT
);

-- Таблица "Машина"
CREATE TABLE car (
                     id SERIAL PRIMARY KEY,
                     brand VARCHAR(50) NOT NULL,
                     model VARCHAR(50) NOT NULL,
                     price DECIMAL(10, 2)
);

-- Связь: внешний ключ на машину
ALTER TABLE person ADD CONSTRAINT fk_person_car
    FOREIGN KEY (car_id) REFERENCES car(id);