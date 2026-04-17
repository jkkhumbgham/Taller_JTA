INSERT INTO examenes (nombre, materia) VALUES ('Examen 1', 'Historia');

INSERT INTO preguntas (enunciado, examen_id) VALUES
    ('¿En qué año llegó Colón a América?', 1),
    ('¿Quién fue el primer presidente de Colombia?', 1);

INSERT INTO opciones (texto, escorrecta, pregunta_id) VALUES
    ('1492',                         true,  1),
    ('1500',                         false, 1),
    ('1485',                         false, 1),
    ('Simón Bolívar',                false, 2),
    ('Francisco de Paula Santander', false, 2),
    ('Antonio Nariño',               true,  2);