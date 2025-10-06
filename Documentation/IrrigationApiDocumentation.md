# Irrigation API Documentation

This is a mock API documentation for app testing. To ensure consistency and completeness, the backend logic for "irrigation control" follows a set of API rules defined here. It uses [Hunter Irrigation](https://www.hunterirrigation.com/sites/default/files/2024-10/Hydrawise%20REST%20API%20Ver%201.6_0.pdf) API documentation as a template.

## Base API
local_machine = "http://192.168.1.133" <br>
API_BASE = "${local_machine}:3000/api/vi

## Manual Start and Stop
## API Actions

| Action   | Description                           | Additional Parameters                          |
|----------|---------------------------------------|------------------------------------------------|
|`run`| Run a specific zone | `zone_id` - Unique zone id|
|`runall` | Run all zones | -|
|`stop`| Stop a specific zone| `zone_id` - Unique zone id|
|`stopall`| Stop all zones| - |

**Examples:** <br>
Running zone 1 <br>
"http://192.168.1.133:3000/api/v1/?action=run&zone_id=1"
<br>

Running all zones<br>
"http://192.168.1.133:3000/api/v1/?action=runall"

## Running on a timer


## Irrigation Schedule



