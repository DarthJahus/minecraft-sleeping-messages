# Sleeping Messages
Announces in chat who is sleeping, with different wording for 1, 2, 3 and many players, and who leaves the bed during the night.

## Examples
When a player gets into a bed:

```text
Jahus_ is sleeping. Sleep too!
Jahus_ and Sayura are sleeping. Sleep too!
Jahus_, Sayura and ATurnipHead are sleeping. Sleep too!
Jahus_ and 3 other heroes are sleeping. Sleep too!
```

With 4 or more sleeping players, only the first one is named; the others are counted.

When the player is alone on the server, a different message can be shown (nothing by default, see `message_sleeping_alone`):

```text
Jahus_ is sleeping and feels lonely ( ˘︹˘ )
```

When every connected player is in bed (2 or more players), `all_players_sleep_message` replaces the end of the sentence (nothing changes if it is empty):

```text
Jahus_ and Sayura are all sleeping. Good night!
```

Player names are colored and hoverable (the tooltip shows the player's profile name).

When a player leaves the bed during the night:

```text
Jahus_ is no longer sleeping!
```

The leave message is not sent when players wake up in the morning, nor when the player is alone on the server.

## Config

`config/sleeping-messages.properties` (created on first run). Missing keys fall back to the defaults listed in the table below; an invalid color name falls back to the default color.

Example configuration:

```properties
message_part_1_singular=is sleeping.
message_part_2=Sleep too!
message_part_1_plural=are sleeping.
joiner=and
msg_other_players=other heroes
message_sleeping_alone=is sleeping and feels lonely ( ˘︹˘ )
all_players_sleep_message=are sleeping. Good night!
player_color=gold
message_color=gray
cooldown_ms=0
enable_left_bed_message=true
message_left_bed=is no longer sleeping!
```

| Key | Default | Description |
| --- | --- | --- |
| `message_part_1_singular` | `is` | Verb used for one sleeping player |
| `message_part_1_plural` | `are` | Verb used for two or more sleeping players |
| `message_part_2` | `sleeping...` | End of the sleeping message |
| `joiner` | `and` | Word joining the last name (or the count) |
| `msg_other_players` | `other players` | Label after the count for 4+ players |
| `message_sleeping_alone` | *(empty)* | Text after the player name when alone on the server. Empty = no message |
| `all_players_sleep_message` | *(empty)* | Text after the names, replacing `message_part_1_plural` + `message_part_2`, when every connected player is in bed (2+ players). Empty = standard message |
| `message_left_bed` | `has left the bed.` | Text after the player name in the leave-bed message |
| `enable_left_bed_message` | `true` | Set to `false` to disable leave-bed messages |
| `player_color` | `gold` | Color of player names (Minecraft color name) |
| `message_color` | `white` | Color of the rest of the message (Minecraft color name) |
| `cooldown_ms` | `0` | Minimum delay in ms between two sleeping announcements (`0` = every time). Does not apply to leave-bed messages |

Message layouts:

```text
1 player:   [p1] [message_part_1_singular] [message_part_2]
2 players:  [p1] [joiner] [p2] [message_part_1_plural] [message_part_2]
3 players:  [p1], [p2] [joiner] [p3] [message_part_1_plural] [message_part_2]
4+ players: [p1] [joiner] <N> [msg_other_players] [message_part_1_plural] [message_part_2]
Alone:      [p1] [message_sleeping_alone]  (nothing if empty)
Everyone:   [names as above] [all_players_sleep_message]  (2+ players, everyone in bed; standard ending if empty)
```