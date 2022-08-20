SELECT (
    nickname,
    currency,
    updated_at,
    created_at
) FROM player_economy WHERE unique_id = ?;
