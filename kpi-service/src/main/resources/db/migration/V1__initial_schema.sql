CREATE TABLE kpis (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_kpi_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'COMPLETED')),
    CONSTRAINT chk_kpi_dates CHECK (end_at > start_at)
);

-- Indexes for kpis table
CREATE INDEX idx_kpi_team_id ON kpis(team_id);
CREATE INDEX idx_kpi_status ON kpis(status);
CREATE INDEX idx_kpi_deleted ON kpis(is_deleted);
CREATE INDEX idx_kpi_start_at ON kpis(start_at);
CREATE INDEX idx_kpi_end_at ON kpis(end_at);

-- Comments for kpis table
COMMENT ON TABLE kpis IS 'Main KPI tracking table with team associations';
COMMENT ON COLUMN kpis.team_id IS 'Foreign key reference to team table in another database';
COMMENT ON COLUMN kpis.status IS 'KPI lifecycle status: DRAFT, ACTIVE, INACTIVE, or COMPLETED';
COMMENT ON COLUMN kpis.is_deleted IS 'Soft delete flag for KPIs';

-- =====================================================
-- Table: kpi_parameters
-- Description: Specific parameters/metrics for each KPI
-- =====================================================
CREATE TABLE kpi_parameters (
    id BIGSERIAL PRIMARY KEY,
    kpi_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_kpi_parameter_kpi FOREIGN KEY (kpi_id) REFERENCES kpis(id) ON DELETE CASCADE
);

-- Indexes for kpi_parameters table
CREATE INDEX idx_kpi_parameter_kpi_id ON kpi_parameters(kpi_id);

-- Comments for kpi_parameters table
COMMENT ON TABLE kpi_parameters IS 'Specific parameters and metrics associated with KPIs';
COMMENT ON COLUMN kpi_parameters.kpi_id IS 'Foreign key reference to kpis table';

-- =====================================================
-- Table: employee_kpi_parameters
-- Description: Tracks employee progress on specific KPI parameters
-- =====================================================
CREATE TABLE employee_kpi_parameters (
    id BIGSERIAL PRIMARY KEY,
    kpi_parameter_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    progress_value INTEGER,
    notes TEXT,
    comment TEXT,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_emp_kpi_param_kpi_parameter FOREIGN KEY (kpi_parameter_id) REFERENCES kpi_parameters(id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_value CHECK (progress_value >= 0 AND progress_value <= 100)
);

-- Indexes for employee_kpi_parameters table
CREATE INDEX idx_emp_kpi_param_kpi_parameter_id ON employee_kpi_parameters(kpi_parameter_id);
CREATE INDEX idx_emp_kpi_param_employee_id ON employee_kpi_parameters(employee_id);
CREATE INDEX idx_emp_kpi_param_composite ON employee_kpi_parameters(kpi_parameter_id, employee_id);

-- Comments for employee_kpi_parameters table
COMMENT ON TABLE employee_kpi_parameters IS 'Tracks individual employee progress on KPI parameters';
COMMENT ON COLUMN employee_kpi_parameters.employee_id IS 'Foreign key reference to employee table in another database';
COMMENT ON COLUMN employee_kpi_parameters.progress_value IS 'Progress percentage value between 0 and 100';

-- =====================================================
-- Table: kpi_audit_logs
-- Description: Audit trail for KPI and parameter changes
-- =====================================================
CREATE TABLE kpi_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    kpi_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    kpi_parameter_id BIGINT,
    old_value JSON,
    new_value JSON,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_kpi_audit_log_kpi FOREIGN KEY (kpi_id) REFERENCES kpis(id) ON DELETE CASCADE,
    CONSTRAINT fk_kpi_audit_log_kpi_parameter FOREIGN KEY (kpi_parameter_id) REFERENCES kpi_parameters(id) ON DELETE SET NULL
);

-- Indexes for kpi_audit_logs table
CREATE INDEX idx_kpi_audit_log_kpi_id ON kpi_audit_logs(kpi_id);
CREATE INDEX idx_kpi_audit_log_employee_id ON kpi_audit_logs(employee_id);
CREATE INDEX idx_kpi_audit_log_kpi_parameter_id ON kpi_audit_logs(kpi_parameter_id);
CREATE INDEX idx_kpi_audit_log_created_at ON kpi_audit_logs(created_at DESC);

-- Comments for kpi_audit_logs table
COMMENT ON TABLE kpi_audit_logs IS 'Audit trail for tracking changes to KPIs and parameters';
COMMENT ON COLUMN kpi_audit_logs.employee_id IS 'Reference to employee who made the change (from another database)';
COMMENT ON COLUMN kpi_audit_logs.old_value IS 'Previous value as JSON';
COMMENT ON COLUMN kpi_audit_logs.new_value IS 'New value as JSON';

-- =====================================================
-- End of Migration V1
-- =====================================================
