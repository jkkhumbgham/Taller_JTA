INSERT INTO examenes (nombre, materia) VALUES ('Examen 2', 'Historia');

INSERT INTO preguntas (enunciado, examen_id) VALUES
    ('¿En qué año llegó Colón a América?', 2),
    ('¿Quién fue el primer presidente de Colombia?', 2);

INSERT INTO opciones (texto, es_correcta, pregunta_id) VALUES
    ('1492', true, 4),
    ('1500', false, 4),
    ('1485', false, 4),

    ('Simón Bolívar', false, 5),
    ('Francisco de Paula Santander', false, 5),
    ('Antonio Nariño', true, 5);