CREATE OR REPLACE VIEW vessel_metrics_statistics AS
SELECT vessel_code,
       -- AVG and STDDEV for power
       AVG(power)                         AS avg_power,
       STDDEV(power)                      AS stddev_power,

       -- AVG and STDDEV for fuel consumption
       AVG(fuel_consumption)              AS avg_fuel_consumption,
       STDDEV(fuel_consumption)           AS stddev_fuel_consumption,

       -- AVG and STDDEV for actual speed overground
       AVG(actual_speed_overground)       AS avg_actual_speed_overground,
       STDDEV(actual_speed_overground)    AS stddev_actual_speed_overground,

       -- AVG and STDDEV for proposed speed overground
       AVG(proposed_speed_overground)     AS avg_proposed_speed_overground,
       STDDEV(proposed_speed_overground)  AS stddev_proposed_speed_overground,

       -- AVG and STDDEV for predicted fuel consumption
       AVG(predicted_fuel_consumption)    AS avg_predicted_fuel_consumption,
       STDDEV(predicted_fuel_consumption) AS stddev_predicted_fuel_consumption,

       -- AVG and STDDEV for speed difference
       AVG(speed_difference)              AS avg_speed_difference,
       STDDEV(speed_difference)           AS stddev_speed_difference,

       -- AVG and STDDEV for fuel efficiency
       AVG(fuel_efficiency)               AS avg_fuel_efficiency,
       STDDEV(fuel_efficiency)            AS stddev_fuel_efficiency

FROM vessel_data
WHERE validation_status = 'VALID'
GROUP BY vessel_code;