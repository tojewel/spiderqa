SELECT Count(*) AS c, t.failure.message as message
FROM   mongo.testaspect.`TestCase` t
WHERE
    started IN (
        SELECT MAX(started) FROM mongo.testaspect.`TestCase` t GROUP BY full_name
    )
    AND t.failure.message IS NOT NULL
GROUP  BY t.failure.message 
ORDER  BY Count(*) DESC
LIMIT 3 OFFSET 2;