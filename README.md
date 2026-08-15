# weather-or-not

MVP core business logic for **Weather or Not**:

- `WeatherImpactEngine`: turns forecast data into an impact level (`NONE`, `LOW`, `MEDIUM`, `HIGH`, `SEVERE`) and additional travel minutes.
- `DecisionEngine`: calculates recommended preparation/departure times based on appointment, safety margin, and weather impact in the relevant travel window.
- configurable impact minutes via `ImpactConfig` (not hardcoded in rules).

## Run tests

```bash
python -m unittest discover -s tests -v
```
