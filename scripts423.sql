-- 1. JOIN-запрос: студенты + факультеты
SELECT s.name AS student_name, s.age AS student_age, f.name AS faculty_name
FROM student s
         INNER JOIN faculty f ON s.faculty_id = f.id;

-- 2. JOIN-запрос: студенты, у которых есть аватарки
SELECT s.id, s.name, s.age, a.file_path
FROM student s
         INNER JOIN avatar a ON s.id = a.student_id;