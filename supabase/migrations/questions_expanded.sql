BEGIN;
SAVEPOINT fix_columns;

-- lessons: missing difficulty + created_at
ALTER TABLE lessons
  ADD COLUMN IF NOT EXISTS difficulty   TEXT CHECK (difficulty IN ('easy', 'medium', 'hard')),
  ADD COLUMN IF NOT EXISTS created_at   TIMESTAMPTZ DEFAULT now();

-- questions: missing correct_answer + created_at + difficulty CHECK
ALTER TABLE questions
  ADD COLUMN IF NOT EXISTS correct_answer  TEXT,
  ADD COLUMN IF NOT EXISTS created_at      TIMESTAMPTZ DEFAULT now();

-- attempts: fix columns to match improved schema
ALTER TABLE attempts
  ADD COLUMN IF NOT EXISTS question_id   UUID REFERENCES questions(id) ON DELETE CASCADE,
  ADD COLUMN IF NOT EXISTS is_correct    BOOLEAN,
  ADD COLUMN IF NOT EXISTS xp_awarded    INT DEFAULT 0,
  ADD COLUMN IF NOT EXISTS attempted_at  TIMESTAMPTZ DEFAULT now();

-- user_stats: timestamps
ALTER TABLE user_stats
  ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ DEFAULT now(),
  ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMPTZ DEFAULT now();

-- languages + achievements: timestamps
ALTER TABLE languages
  ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ DEFAULT now();

ALTER TABLE achievements
  ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ DEFAULT now();

RELEASE SAVEPOINT fix_columns;
COMMIT;

BEGIN;
SAVEPOINT questions_insert;

INSERT INTO questions (
  id, lesson_id, type, prompt, options, correct_answer_index,
  xp_reward, code_snippet, explanation, difficulty
) VALUES
  ('d133f369-88b0-4eb3-9326-ba210f5323b5', 'f622c48e-a628-4e48-80f9-071b33373f52', 'multiple_choice', 'What is the result of bool(0) in Python?', ARRAY['True','False','0','None'], 1, 10, NULL, '0 is falsy in Python. bool(0) returns False. All non-zero numbers are truthy.', 'easy'),
  ('105c06b8-af8b-45ee-b8f7-02a03baf1173', 'f622c48e-a628-4e48-80f9-071b33373f52', 'true_false', 'In Python, None is the same as False.', ARRAY['True','False'], 1, 10, NULL, 'None and False are different objects. bool(None) is False, but None is not False.', 'easy'),
  ('c823e7a6-66c3-4423-bc3e-2b653414d775', 'f622c48e-a628-4e48-80f9-071b33373f52', 'multiple_choice', 'Which of the following is a valid Python complex number?', ARRAY['3+4j','3+4i','complex(3,4i)','(3,4)'], 0, 10, NULL, 'Python uses j (not i) for the imaginary part. 3+4j is a valid complex literal.', 'medium'),
  ('c3011f6b-c745-4fa1-970f-1498c470793a', 'f622c48e-a628-4e48-80f9-071b33373f52', 'true_false', 'int(''3.14'') raises a ValueError in Python.', ARRAY['True','False'], 0, 10, NULL, 'int() cannot directly parse a float string like ''3.14''. Use float(''3.14'') first, or int(float(''3.14'')).', 'medium'),
  ('8037e0c7-f7a2-4943-8d0d-a52787eea332', 'f622c48e-a628-4e48-80f9-071b33373f52', 'code_snippet', 'What is the output?', ARRAY['<class int>','<class float>','<class bool>','<class str>'], 2, 10, 'x = True
print(type(x))', 'True is a bool in Python. bool is a subclass of int, but type() reports the most specific type: bool.', 'easy'),
  ('841650d6-4987-446b-9f8d-70c0c6bb9e0c', 'f622c48e-a628-4e48-80f9-071b33373f52', 'code_snippet', 'What does this print?', ARRAY['True','False','1','Error'], 0, 20, 'print(isinstance(True, int))', 'bool is a subclass of int in Python, so isinstance(True, int) is True.', 'medium'),
  ('3d894e93-28d2-499e-aeff-886d4671deb7', 'f622c48e-a628-4e48-80f9-071b33373f52', 'multiple_choice', 'What is the output of round(2.5) in Python 3?', ARRAY['3','2','2.5','Error'], 1, 20, NULL, 'Python 3 uses banker''s rounding (round half to even). 2.5 rounds to 2 (nearest even). 3.5 rounds to 4.', 'hard'),
  ('fb2322b3-5f3d-4dae-8894-337546fbf4df', 'f622c48e-a628-4e48-80f9-071b33373f52', 'true_false', 'In Python 3, dividing two integers with / always returns a float.', ARRAY['True','False'], 0, 10, NULL, 'Correct — / performs true division and always returns float. Use // for integer (floor) division.', 'easy'),
  ('087fe34f-82a6-4bd9-911d-517e366d4032', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'multiple_choice', 'Which method removes leading and trailing whitespace from a string?', ARRAY['.strip()','.trim()','.clean()','.lstrip()'], 0, 10, NULL, '.strip() removes whitespace from both ends. .lstrip() only removes from the left, .rstrip() from the right.', 'easy'),
  ('fd0e2d2a-04ec-45fb-a46c-d8cb23c3ac52', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'true_false', 'f-strings were introduced in Python 3.6.', ARRAY['True','False'], 0, 10, NULL, 'Correct — f-strings became available in Python 3.6 via PEP 498.', 'easy'),
  ('557f9d7c-40f2-4792-9685-0ee27260a44b', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'multiple_choice', 'What does ''Python''[1:4] return?', ARRAY['''Pyt''','''yth''','''ytho''','''ython'''], 1, 10, NULL, 'Slicing is start-inclusive, end-exclusive. Index 1=y, 2=t, 3=h → yth.', 'medium'),
  ('f153139d-ec07-4d20-8a30-e438f4b44b5e', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'code_snippet', 'What is the output?', ARRAY['3','2','4','Error'], 0, 10, 's = ''banana''
print(s.count(''a''))', '''banana'' contains ''a'' at indices 1, 3, 5 — three occurrences.', 'easy'),
  ('835c33cc-11d5-4dbd-b827-237a21a5ce27', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'code_snippet', 'What is printed?', ARRAY['''Hello World''','''hello world''','''Hello world''','Error'], 2, 20, 's = ''hello world''
print(s.capitalize())', 'capitalize() uppercases only the first character and lowercases the rest.', 'medium'),
  ('67de776c-10f5-4ed6-9609-0c8ab064de4e', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'multiple_choice', 'Which of the following creates a multi-line string in Python?', ARRAY['''line1\nline2''','"""line1\nline2"""','multi(''line1'',''line2'')','All of the above'], 3, 10, NULL, 'Both escape sequences (\n) and triple-quoted strings produce multi-line strings. Both approaches are valid.', 'medium'),
  ('5a827ce1-2d42-4eb4-ab3b-a7e4d01cc156', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'code_snippet', 'What does this print?', ARRAY['True','False','Error','None'], 0, 10, 'print(''python''.startswith(''py''))', 'startswith() returns True if the string begins with the given prefix.', 'easy'),
  ('9b123584-fa15-48d1-84d6-15db53480b23', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'multiple_choice', 'What is the result of ''-''.join([''a'',''b'',''c''])?', ARRAY['[''a-b-c'']','''a-b-c''','''a'',''b'',''c''','Error'], 1, 10, NULL, 'str.join(iterable) concatenates the elements with the string as separator → a-b-c.', 'easy'),
  ('152616a3-d677-4b5f-858b-3d05b427660c', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'code_snippet', 'What is the output?', ARRAY['''  hello  ''','''hello''','''hello  ''','''  hello'''], 1, 10, 's = ''  hello  ''
print(s.strip())', '.strip() removes leading and trailing whitespace from both ends.', 'easy'),
  ('8a280a0c-2069-4c14-85e8-680439ae3628', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'true_false', 'Python strings support negative indexing.', ARRAY['True','False'], 0, 10, NULL, 'Correct — s[-1] gives the last character, s[-2] the second-last, etc.', 'easy'),
  ('4c06c150-0cde-4a96-a50e-8881784d1be1', 'aa85704e-1f44-4fbb-a7f1-bec11e4e5b15', 'code_snippet', 'What is printed?', ARRAY['''PythonPythonPython''','''Python * 3''','Error','''PPP'''], 0, 10, 'print(''Python'' * 3)', 'The * operator repeats a string. ''Python'' * 3 → ''PythonPythonPython''.', 'easy'),
  ('b2b35839-f98c-47c9-baca-93d2434713f5', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'multiple_choice', 'How do you add an element to the END of a list?', ARRAY['.add(x)','.append(x)','.push(x)','.insert(x)'], 1, 10, NULL, '.append(x) adds x to the end of the list in-place. .insert(i, x) inserts at index i.', 'easy'),
  ('83dc60a7-7188-4892-8604-9030cfaa05de', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'true_false', 'list.sort() returns a new sorted list.', ARRAY['True','False'], 1, 10, NULL, 'list.sort() sorts IN-PLACE and returns None. Use sorted(list) to get a new sorted list.', 'medium'),
  ('ff6ddc10-7bf9-47a6-9376-678de2bbb102', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'multiple_choice', 'What does list(range(0, 10, 2)) produce?', ARRAY['[0,2,4,6,8]','[0,2,4,6,8,10]','[2,4,6,8]','[1,3,5,7,9]'], 0, 10, NULL, 'range(start, stop, step) generates numbers from 0 up to (not including) 10 in steps of 2.', 'easy'),
  ('d5f2d338-006d-4c43-9817-07711157289c', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'code_snippet', 'What is the output?', ARRAY['[1,2,3,4,5,6]','[[1,2,3],[4,5,6]]','Error','[1,2,3,[4,5,6]]'], 0, 10, 'a = [1, 2, 3]
b = [4, 5, 6]
print(a + b)', 'The + operator concatenates lists, producing a single flat list [1,2,3,4,5,6].', 'easy'),
  ('6342829d-f1f8-48bd-aa54-49c552233b4d', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'multiple_choice', 'Which method removes the FIRST occurrence of a value from a list?', ARRAY['.pop(x)','.remove(x)','.delete(x)','.discard(x)'], 1, 10, NULL, '.remove(x) finds and removes the first occurrence of x. .pop(i) removes by index. .discard() is for sets.', 'medium'),
  ('ac52be15-ac10-4666-ae49-fc79b66b72ad', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'code_snippet', 'What is the output?', ARRAY['[3,2,1]','[1,2,3]','None','Error'], 0, 10, 'lst = [1, 2, 3]
lst.reverse()
print(lst)', '.reverse() reverses the list in-place. The list itself is modified to [3,2,1].', 'easy'),
  ('266d2925-bd90-4c50-9e5d-169063de4e24', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'true_false', 'Slicing a list creates a shallow copy of the sliced portion.', ARRAY['True','False'], 0, 20, NULL, 'Correct — list slices produce shallow copies. Nested objects are shared by reference, not deeply copied.', 'hard'),
  ('1c2941ec-52f9-4d00-b3db-a58330104eb5', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'code_snippet', 'What is printed?', ARRAY['[0,0,0,0,0]','[0,1,2,3,4]','[[0],[1],[2],[3],[4]]','Error'], 0, 10, 'result = [0] * 5
print(result)', '[0] * 5 repeats the list five times, producing five separate 0 values.', 'easy'),
  ('44da2938-4e66-4713-ba25-8a422d58210a', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'multiple_choice', 'What is the time complexity of accessing an element by index in a Python list?', ARRAY['O(n)','O(log n)','O(1)','O(n²)'], 2, 20, NULL, 'Python lists are backed by dynamic arrays. Index access is O(1) — direct memory offset calculation.', 'hard'),
  ('a15fbf50-2e6e-46ae-aa7f-7b7cdbf26b3f', '2659c071-79f7-4a57-ba7a-15b6b1e8a887', 'code_snippet', 'What does this produce?', ARRAY['[1,3,5,7,9]','[0,2,4,6,8]','[1,2,3,4,5]','Error'], 0, 20, 'evens = [x for x in range(10) if x % 2 != 0]
print(evens)', 'The condition x % 2 != 0 filters for odd numbers: 1,3,5,7,9.', 'medium'),
  ('145ddab6-7088-44b1-a0f9-db75c191e11c', '24b5cfc5-408c-4f46-8db9-343005598ede', 'multiple_choice', 'Which method returns all keys of a dictionary?', ARRAY['.keys()','.values()','.items()','.get()'], 0, 10, NULL, '.keys() returns a dict_keys view of all keys. .values() returns values, .items() returns (key,value) pairs.', 'easy'),
  ('b5f2d214-a54f-4d8d-a8fb-eb79426d649c', '24b5cfc5-408c-4f46-8db9-343005598ede', 'true_false', 'As of Python 3.7+, dictionaries maintain insertion order.', ARRAY['True','False'], 0, 10, NULL, 'Correct — from Python 3.7 the dict preserves insertion order as part of the language spec.', 'medium'),
  ('889f8ce5-82c6-40d7-bc80-74b4a2ac802e', '24b5cfc5-408c-4f46-8db9-343005598ede', 'code_snippet', 'What is the output?', ARRAY['KeyError','None','0','missing'], 3, 10, 'd = {''a'': 1}
print(d.get(''b'', ''missing''))', '.get(key, default) returns the default when the key is absent. ''b'' is not in d → missing.', 'easy'),
  ('fcb7a04b-6178-476a-b75d-4f0911d895e1', '24b5cfc5-408c-4f46-8db9-343005598ede', 'multiple_choice', 'How do you remove a key-value pair from a dict and return its value?', ARRAY['dict.remove(key)','del dict[key]','dict.pop(key)','dict.discard(key)'], 2, 10, NULL, '.pop(key) removes the key and returns its value. del dict[key] removes it but returns nothing.', 'medium'),
  ('6a1c14b2-4dd0-4e16-ac7c-218bda48b65f', '24b5cfc5-408c-4f46-8db9-343005598ede', 'code_snippet', 'What is printed?', ARRAY['{a:1,b:2,c:3}','{a:1,b:99,c:3}','{b:99,c:3}','Error'], 1, 10, 'd = {''a'': 1, ''b'': 2, ''c'': 3}
d[''b''] = 99
print(d)', 'Assigning to an existing key updates its value in-place. b changes from 2 to 99.', 'easy'),
  ('ef534465-6316-4618-b402-30bdad99a071', '24b5cfc5-408c-4f46-8db9-343005598ede', 'multiple_choice', 'What does dict.setdefault(key, value) do?', ARRAY['Always sets the key to value','Sets key to value only if key is not already in the dict','Deletes the key if it equals value','Returns value without modifying the dict'], 1, 20, NULL, '.setdefault(key, val) inserts key with val only when the key is absent.', 'hard'),
  ('7f0f11e2-a1f9-4222-b589-a8a0edc9963a', '24b5cfc5-408c-4f46-8db9-343005598ede', 'true_false', 'A dictionary can use a list as a key.', ARRAY['True','False'], 1, 10, NULL, 'Dictionary keys must be hashable. Lists are mutable and not hashable — using one raises a TypeError.', 'medium'),
  ('471c0c99-b70a-4db4-af2b-b3d18afabe87', '24b5cfc5-408c-4f46-8db9-343005598ede', 'code_snippet', 'What is the output?', ARRAY['3','2','1','Error'], 0, 10, 'd = {''x'': 1, ''y'': 2, ''z'': 3}
print(len(d))', 'len() on a dict returns the number of key-value pairs. d has 3 pairs.', 'easy'),
  ('d5a727c6-8f97-4587-bbdf-ad5fae2c3544', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'multiple_choice', 'What does **kwargs allow in a function?', ARRAY['Any number of positional args','Any number of keyword arguments as a dict','A required keyword argument','A default argument'], 1, 10, NULL, '**kwargs collects extra keyword arguments into a dictionary. *args collects extra positional arguments into a tuple.', 'medium'),
  ('ee85bbdf-eccc-487b-8dbd-68f26159bcd2', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'true_false', 'Default parameter values in Python are evaluated once when the function is defined, not each time it is called.', ARRAY['True','False'], 0, 20, NULL, 'Correct! Using mutable defaults like def f(lst=[]) is a classic bug — the same list object is reused across calls.', 'hard'),
  ('87e7fdb3-4cd7-41bb-ba2d-fe2eb9bed88d', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'code_snippet', 'What is the output?', ARRAY['25','10','Error','None'], 0, 10, 'square = lambda x: x ** 2
print(square(5))', 'The lambda takes x and returns x**2. square(5) → 25.', 'easy'),
  ('24fcd270-4f77-4df1-a98e-017c9ebcd00a', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'multiple_choice', 'Which built-in function applies a function to each item of an iterable?', ARRAY['apply()','filter()','map()','reduce()'], 2, 10, NULL, 'map(func, iterable) applies func to every item and returns an iterator of results.', 'medium'),
  ('c377c41f-0c60-4be6-a34c-85110d88a0d4', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'code_snippet', 'What is printed?', ARRAY['[2,4,6]','[1,2,3]','<filter object>','Error'], 0, 20, 'nums = [1, 2, 3, 4, 5, 6]
evens = list(filter(lambda x: x % 2 == 0, nums))
print(evens)', 'filter() keeps items where the function returns True. Even numbers from 1-6 are 2, 4, 6.', 'medium'),
  ('f6ce4dd0-c29e-42e0-825c-27b8156ed309', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'true_false', 'A Python function can return multiple values as a tuple.', ARRAY['True','False'], 0, 10, NULL, 'Correct — return a, b returns a tuple (a, b). You can unpack it with x, y = func().', 'easy'),
  ('0768c4c2-f639-4cf0-85c0-a45591134f92', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'multiple_choice', 'What is a decorator in Python?', ARRAY['A design pattern class','A function that wraps another function to extend its behaviour','A built-in type for styling','A comment syntax'], 1, 20, NULL, 'A decorator is a callable that takes a function, wraps it, and returns the enhanced version. Used with @syntax.', 'hard'),
  ('76b4a5e0-d6d0-4dc0-be74-0c8e5b2d6ce3', 'aadbad27-5713-40e3-9fed-8c85beb4739d', 'code_snippet', 'What is the output?', ARRAY['1','2','3','[1,2,3]'], 2, 20, 'def last(*args):
    return args[-1]

print(last(1, 2, 3))', '*args collects all positional args into a tuple (1,2,3). args[-1] is the last element: 3.', 'medium'),
  ('2ce73b32-43db-46e1-b225-3e5cc5e92640', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'multiple_choice', 'What is the output of typeof undefined?', ARRAY['null','undefined','object','void'], 1, 10, NULL, 'typeof undefined returns the string undefined. Note: typeof null returns object (a historical bug).', 'easy'),
  ('c6cd4a5b-f04a-466f-bbcf-38b94e02871c', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'true_false', 'var declarations are function-scoped, not block-scoped.', ARRAY['True','False'], 0, 10, NULL, 'Correct — var is hoisted to the nearest function scope. let and const are block-scoped.', 'medium'),
  ('4d2db197-a291-4be3-87c5-fcdd7bfcdd20', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'multiple_choice', 'What does NaN === NaN evaluate to in JavaScript?', ARRAY['true','false','TypeError','undefined'], 1, 20, NULL, 'NaN is the only JavaScript value not equal to itself. Use Number.isNaN() to check for NaN.', 'hard'),
  ('763e9ccd-44d2-4af1-8262-c72c1658098d', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'code_snippet', 'What is the output?', ARRAY['2','''2''','NaN','undefined'], 0, 20, 'console.log(''6'' - 4);', 'The - operator is numeric-only, so 6 is coerced to a number. 6 - 4 = 2.', 'medium'),
  ('b680ecc3-4558-4e62-8d8a-5a1d0fd933fc', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'multiple_choice', 'Which method converts a JSON string to a JavaScript object?', ARRAY['JSON.decode()','JSON.parse()','JSON.fromString()','JSON.objectify()'], 1, 10, NULL, 'JSON.parse(str) parses a JSON string and returns the corresponding JavaScript value/object.', 'easy'),
  ('a39b54e1-9b59-4b17-8c29-3aaeb42a6f26', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'true_false', '0 == false is true in JavaScript.', ARRAY['True','False'], 0, 10, NULL, 'Correct — == performs type coercion. 0 is coerced to false, so 0 == false is true. Use === to avoid this.', 'medium'),
  ('d03ca48b-c7a9-4bae-bd8f-d8bd1245ed1d', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'code_snippet', 'What is logged?', ARRAY['1 2 3','[1,2,3]','''1,2,3''','Error'], 0, 20, 'const arr = [1, 2, 3];
console.log(...arr);', 'The spread operator expands the array into individual arguments. console.log(1, 2, 3) prints them space-separated.', 'medium'),
  ('f2bc446d-cefb-476f-a771-000b62eb0187', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'multiple_choice', 'What does Array.isArray([1,2,3]) return?', ARRAY['false','true','''array''','undefined'], 1, 10, NULL, 'Array.isArray() is the reliable way to check if a value is an array. typeof [] returns object.', 'easy'),
  ('3512a470-130e-4758-9a2c-c558d81efa5e', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'code_snippet', 'What is the output?', ARRAY['undefined','ReferenceError','5','null'], 0, 20, 'console.log(x);
var x = 5;', 'var declarations are hoisted but not their assignments. At the time of the log, x is declared but undefined.', 'hard'),
  ('d6ac9120-1101-4102-a578-b102144185d0', 'b8cf5190-8e93-476f-aa45-f7fa77baa5c0', 'true_false', 'null == undefined is true in JavaScript.', ARRAY['True','False'], 0, 20, NULL, 'Correct — null and undefined are loosely equal with ==, but null !== undefined with ===.', 'hard'),
  ('c52dd1e1-1afb-401a-bfe2-1f7acbf93032', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'multiple_choice', 'What does Promise.all() do when one promise rejects?', ARRAY['Ignores the rejection and continues','Immediately rejects with that error','Waits for all promises then reports errors','Returns undefined'], 1, 20, NULL, 'Promise.all() short-circuits on the first rejection. Use Promise.allSettled() to wait for all regardless of outcome.', 'hard'),
  ('9ae5787a-6243-4b75-b968-9071c334282e', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'true_false', 'async functions always return a Promise.', ARRAY['True','False'], 0, 10, NULL, 'Correct — an async function implicitly wraps its return value in a resolved Promise.', 'medium'),
  ('40cbebe6-22ce-406a-9e95-e91ecaf82b79', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'code_snippet', 'What is the output?', ARRAY['1','2','one','Error'], 0, 20, 'const obj = { a: 1, b: 2 };
const { a } = obj;
console.log(a);', 'Destructuring extracts named properties. const { a } pulls out obj.a which is 1.', 'medium'),
  ('cc1ff74b-0f27-4efe-b12f-8a9da326d609', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'multiple_choice', 'What is event bubbling in JavaScript?', ARRAY['Events that fire repeatedly','An event propagating up from the target element to its ancestors','Memory leaks from event listeners','Asynchronous event queue processing'], 1, 20, NULL, 'When an event fires on an element, it bubbles up through parent elements. Use event.stopPropagation() to prevent this.', 'hard'),
  ('c12347ea-8804-4463-bf87-7dc6411f087e', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'code_snippet', 'What is logged?', ARRAY['3','three','undefined','Error'], 0, 20, 'const arr = [1, 2, 3];
const [,, last] = arr;
console.log(last);', 'Array destructuring with commas to skip elements. The two commas skip 1 and 2; last gets 3.', 'hard'),
  ('33214a86-c888-4cfb-8384-7586df192218', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'true_false', 'The spread operator can be used to merge two arrays.', ARRAY['True','False'], 0, 10, NULL, 'Correct — const merged = [...arr1, ...arr2] creates a new array containing all elements of both arrays.', 'easy'),
  ('599e7ac1-be78-4e4b-9b86-30cc43b903af', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'multiple_choice', 'What does Array.prototype.flatMap() do?', ARRAY['Filters then maps','Maps then flattens one level','Deep-flattens nested arrays','Sorts and maps'], 1, 20, NULL, 'flatMap() is equivalent to .map().flat(1) — it maps over elements and flattens the result by one level.', 'hard'),
  ('2a7a6bca-b7ed-4122-86c5-50e4e2cd4de9', '6eab50da-cc98-432b-b7e7-ee2164cb9996', 'code_snippet', 'What is the output?', ARRAY['5','NaN','undefined','TypeError'], 0, 20, 'const add = (a, b = 3) => a + b;
console.log(add(2));', 'b has a default value of 3. Calling add(2) uses a=2, b=3 → 2+3 = 5.', 'medium'),
  ('c9736115-96ad-4670-b36c-ba058abc7f27', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'multiple_choice', 'What is the difference between val and var in Kotlin?', ARRAY['val is public, var is private','val is immutable (read-only), var is mutable','val is for primitives, var is for objects','There is no difference'], 1, 10, NULL, 'val declares a read-only reference (like final in Java). var declares a mutable variable that can be reassigned.', 'easy'),
  ('477f10fa-077c-41f2-a889-fa1bcb55b68d', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'true_false', 'Kotlin has a primitive int type separate from the Int class.', ARRAY['True','False'], 1, 20, NULL, 'In Kotlin source code you always use Int. The compiler optimises to JVM primitives where possible.', 'hard'),
  ('2fb976a5-8df6-4a62-a8f3-56e4e62ba590', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'code_snippet', 'What is the output?', ARRAY['''Hello, World!''','Hello, World!','Error','null'], 1, 10, 'val name = "World"
println("Hello, $name!")', 'String templates use $ to embed variables. $name is replaced with World at runtime.', 'easy'),
  ('660ed444-4215-4d2c-8b2f-04639c4d6b81', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'multiple_choice', 'What does the safe call operator ?. do in Kotlin?', ARRAY['Throws NullPointerException if null','Calls the method only if the receiver is not null, otherwise returns null','Converts nullable to non-null','Asserts non-null at compile time'], 1, 10, NULL, '?. short-circuits: if the left side is null it returns null immediately without calling the method.', 'medium'),
  ('0e8d69d8-e00e-49f6-8dbd-c385ac8f0149', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'code_snippet', 'What is printed?', ARRAY['10','5','null','Error'], 0, 10, 'val x: Int? = null
val result = x ?: 10
println(result)', 'x is null, so the Elvis operator ?: returns the right-hand default value: 10.', 'easy'),
  ('bc03f530-5a15-438b-8057-fc2a14bedb0c', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'true_false', 'In Kotlin, String is nullable by default.', ARRAY['True','False'], 1, 10, NULL, 'In Kotlin all types are non-nullable by default. String cannot hold null. You need String? to allow null.', 'easy'),
  ('7ad10cfc-50d2-4cf0-8c19-ccbdfb02662e', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'multiple_choice', 'Which keyword is used to define an interface in Kotlin?', ARRAY['abstract','trait','interface','protocol'], 2, 10, NULL, 'Kotlin uses the interface keyword, same as Java. Interfaces can have default implementations in Kotlin.', 'easy'),
  ('c0aea247-e900-43d4-b3e8-792058eb0df4', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'code_snippet', 'What is the output?', ARRAY['true','false','Error','null'], 0, 10, 'val list = listOf(1, 2, 3)
println(2 in list)', 'The in operator checks membership. 2 is in the list → true.', 'easy'),
  ('6d78d038-d277-42df-8d7a-f9c5dec1b1a4', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'multiple_choice', 'What does when replace in Kotlin compared to Java?', ARRAY['if-else','for loop','try-catch','switch statement'], 3, 10, NULL, 'Kotlin''s when is an enhanced replacement for Java''s switch. It can be used as both a statement and an expression.', 'easy'),
  ('3f7c6789-b114-4d9c-85a1-f82d045e2110', '23e31af5-7a05-4e4c-9cbb-db4768d11928', 'code_snippet', 'What is printed?', ARRAY['even','odd','zero','Error'], 0, 20, 'val n = 4
val result = when {
    n % 2 == 0 -> "even"
    else       -> "odd"
}
println(result)', 'n=4 satisfies n % 2 == 0, so when returns even.', 'medium'),
  ('cd446dae-b169-4bb5-94f1-386ead5a10bc', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'multiple_choice', 'What does the filter function do on a Kotlin collection?', ARRAY['Transforms each element','Returns elements matching a predicate','Sorts the collection','Reduces to a single value'], 1, 10, NULL, 'filter { predicate } returns a new list containing only elements for which the predicate returns true.', 'easy'),
  ('427a1fdd-1fa7-413e-8013-d5015ba61e00', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'code_snippet', 'What is the output?', ARRAY['[2,4,6]','[1,3,5]','[1,2,3,4,5,6]','Error'], 0, 10, 'val nums = listOf(1, 2, 3, 4, 5, 6)
val evens = nums.filter { it % 2 == 0 }
println(evens)', 'filter keeps elements where it % 2 == 0 (even numbers): 2, 4, 6.', 'easy'),
  ('59ee2b04-09bf-4b12-b012-ac5d908ca882', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'multiple_choice', 'What does map do on a Kotlin collection?', ARRAY['Filters elements','Transforms each element and returns a new list','Finds an element','Sorts the list'], 1, 10, NULL, 'map { transform } applies the lambda to each element and returns a new list of the results.', 'easy'),
  ('e4e70681-f83b-4594-9ef7-0d1b0e9c96c8', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'code_snippet', 'What is printed?', ARRAY['[1,4,9]','[1,2,3]','[2,4,6]','Error'], 0, 10, 'val nums = listOf(1, 2, 3)
val squared = nums.map { it * it }
println(squared)', 'map squares each element: 1→1, 2→4, 3→9 → [1, 4, 9].', 'easy'),
  ('008207cf-f5f1-4db5-9719-130a19401797', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'multiple_choice', 'What does fold do in Kotlin?', ARRAY['Flattens nested lists','Accumulates a value across a collection starting from an initial value','Filters and maps simultaneously','Groups elements by a key'], 1, 20, NULL, 'fold(initial) { acc, element -> ... } reduces the collection to a single value, starting from the initial accumulator.', 'hard'),
  ('55154209-b72c-4064-91ea-adcebc604b6c', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'code_snippet', 'What is the output?', ARRAY['15','0','[1,2,3,4,5]','Error'], 0, 20, 'val nums = listOf(1, 2, 3, 4, 5)
val sum = nums.fold(0) { acc, n -> acc + n }
println(sum)', 'fold starts at 0 and adds each element: 0+1+2+3+4+5 = 15.', 'medium'),
  ('a8fd7744-a507-4c2a-bcf5-1793be7d5b05', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'true_false', 'In Kotlin, you can store a lambda in a variable and pass it as an argument.', ARRAY['True','False'], 0, 10, NULL, 'Correct — lambdas are first-class values in Kotlin.', 'medium'),
  ('6ec98963-ab79-434a-85bd-7871f59f3f46', '48fd1d1d-1a3a-4c36-8a63-223f028dbb2d', 'multiple_choice', 'What does the let scope function return?', ARRAY['The receiver object','The result of the lambda','Unit','Nothing'], 1, 20, NULL, 'let executes the lambda and returns the lambda''s result. also and apply return the receiver object instead.', 'hard'),
  ('1ef65fcd-f3f8-4d6d-ba29-fe109f6f47b2', '14d20e76-768d-41e5-82e4-ded8bea336d6', 'multiple_choice', 'What does the override keyword do in Kotlin?', ARRAY['Hides the parent method','Explicitly marks a method as overriding a parent method','Makes a method final','Declares an abstract method'], 1, 10, NULL, 'override is mandatory in Kotlin when overriding a parent method. The compiler enforces it, preventing accidental overrides.', 'medium'),
  ('e41ef48a-eed5-4f04-9c20-a39d087dd1ae', '14d20e76-768d-41e5-82e4-ded8bea336d6', 'true_false', 'An abstract class in Kotlin can be instantiated directly.', ARRAY['True','False'], 1, 10, NULL, 'Abstract classes cannot be instantiated. You must create a concrete subclass that implements all abstract members.', 'easy'),
  ('7e9db9bf-6833-47d1-a982-5da7e3b41ef2', '14d20e76-768d-41e5-82e4-ded8bea336d6', 'code_snippet', 'What is the output?', ARRAY['Animal speaks','Dog barks','Error','null'], 1, 20, 'open class Animal {
    open fun speak() = "Animal speaks"
}
class Dog : Animal() {
    override fun speak() = "Dog barks"
}
println(Dog().speak())', 'Dog overrides speak(). Calling speak() on a Dog instance returns Dog barks.', 'medium'),
  ('5b157322-b3ee-4ac8-8e23-e847aaf67aa6', '14d20e76-768d-41e5-82e4-ded8bea336d6', 'multiple_choice', 'What is an object declaration in Kotlin?', ARRAY['An anonymous class instance','A singleton — a class with exactly one instance created lazily','A data class with no fields','A companion object'], 1, 20, NULL, 'object MyObject { } creates a singleton. The instance is created lazily on first access.', 'hard'),
  ('be228a82-fc02-40ec-9c36-cba35948f35e', '14d20e76-768d-41e5-82e4-ded8bea336d6', 'true_false', 'A companion object in Kotlin is equivalent to static members in Java.', ARRAY['True','False'], 0, 10, NULL, 'Correct — companion object members can be called on the class name, like static in Java.', 'medium'),
  ('44f1cd3d-e979-40a0-a12a-41c141aed5b1', '14d20e76-768d-41e5-82e4-ded8bea336d6', 'multiple_choice', 'What is the primary constructor in Kotlin?', ARRAY['The first function in the class body','The constructor defined in the class header','A constructor with no parameters','The constructor called super()'], 1, 10, NULL, 'The primary constructor is declared in the class header: class Person(val name: String).', 'medium'),
  ('5b633c02-02eb-40d3-a378-b15ba9076ac5', 'f5329c88-f286-46b1-9f33-2a493ddac120', 'multiple_choice', 'How many instances can an object subclass of a sealed class have?', ARRAY['Unlimited','Zero','Exactly one','Two'], 2, 20, NULL, 'When a sealed class subclass is declared as object, it is a singleton — there is exactly one instance.', 'hard'),
  ('0cc90baa-df12-4150-a5bb-c9d3aafd354b', 'f5329c88-f286-46b1-9f33-2a493ddac120', 'code_snippet', 'What is the output?', ARRAY['Success','Failure','Loading','Error'], 0, 20, 'sealed class State
object Success : State()
object Failure : State()

val s: State = Success
val msg = when(s) {
    is Success -> "Success"
    is Failure -> "Failure"
}
println(msg)', 's is the Success singleton. The when branch matches Success.', 'medium'),
  ('4d4bf897-4094-4653-b166-1cd38eb310d9', 'f5329c88-f286-46b1-9f33-2a493ddac120', 'true_false', 'Kotlin enum entries can have different sets of properties.', ARRAY['True','False'], 1, 20, NULL, 'All enum entries share the same set of properties defined in the enum class. For different shapes of data, use sealed classes.', 'hard'),
  ('7504f62b-8403-4435-88b3-6c8f9915218a', 'ff1b6d31-5a6e-4bbb-b7cd-f08ee065336d', 'multiple_choice', 'What is the difference between launch and async in Kotlin coroutines?', ARRAY['launch is for UI, async is for I/O','launch returns Unit (fire-and-forget), async returns Deferred with a result','launch is synchronous, async is not','They are identical'], 1, 20, NULL, 'launch is used when you do not need a result. async returns a Deferred<T> whose value you retrieve with .await().', 'hard'),
  ('33feabba-4b72-40e9-9d2a-ac0e169bc786', 'ff1b6d31-5a6e-4bbb-b7cd-f08ee065336d', 'true_false', 'delay() in Kotlin coroutines blocks the underlying thread.', ARRAY['True','False'], 1, 10, NULL, 'delay() is a suspending function — it suspends the coroutine without blocking the thread.', 'medium'),
  ('251741cb-99fa-4543-b61f-b874f1eb1a46', 'ff1b6d31-5a6e-4bbb-b7cd-f08ee065336d', 'multiple_choice', 'What scope function is typically used in Android ViewModels to launch coroutines?', ARRAY['GlobalScope','runBlocking','viewModelScope','CoroutineScope(Dispatchers.IO)'], 2, 20, NULL, 'viewModelScope is lifecycle-aware and automatically cancels coroutines when the ViewModel is cleared, preventing leaks.', 'hard'),
  ('a94e9b5c-bdd6-43d8-9942-4c80ef8b0e2c', 'ff1b6d31-5a6e-4bbb-b7cd-f08ee065336d', 'code_snippet', 'What is the minimum output order?', ARRAY['A then B','B then A','Only A','Only B'], 0, 20, 'import kotlinx.coroutines.*
fun main() = runBlocking {
    println("A")
    delay(100)
    println("B")
}', 'runBlocking runs sequentially. A is printed, then after 100ms delay B is printed.', 'medium'),
  ('3603b905-2469-4350-8d3a-35e6e218b91f', 'ff1b6d31-5a6e-4bbb-b7cd-f08ee065336d', 'true_false', 'withContext() changes the coroutine dispatcher without launching a new coroutine.', ARRAY['True','False'], 0, 20, NULL, 'Correct — withContext(dispatcher) switches the dispatcher for its block and suspends until the block completes.', 'hard');

RELEASE SAVEPOINT questions_insert;
COMMIT;
