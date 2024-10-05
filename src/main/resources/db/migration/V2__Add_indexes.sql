CREATE INDEX idx_vessel_code_validation_status ON vessel_data (vessel_code, validation_status);
CREATE INDEX idx_vessel_code_on_validation_error ON vessel_data_validation_errors (vessel_code);
CREATE INDEX idx_vessel_code_problem_type_on_validation_error ON vessel_data_validation_errors (vessel_code, problem_type);


