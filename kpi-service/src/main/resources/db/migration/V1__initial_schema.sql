-- =====================================================
-- Table: employees
-- Description: Employee data for KPI management system
-- =====================================================
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    partner_id BIGINT,
    supervisor_id VARCHAR(50),
    date_of_birth TIMESTAMP,
    joining_date TIMESTAMP,
    designation VARCHAR(255),
    gender VARCHAR(20),
    nid VARCHAR(50),
    tin_number VARCHAR(50),
    blood_group VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_employee_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT chk_employee_blood_group CHECK (blood_group IN ('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE')),
    CONSTRAINT chk_employee_status CHECK (status IN ('STAGE1', 'STAGE2', 'CONSULTANT', 'CONTRACTUAL', 'PERMANENT', 'TERMINATED'))
);

-- Indexes for employees table
CREATE INDEX idx_employee_employee_id ON employees(employee_id);
CREATE INDEX idx_employee_email ON employees(email);
CREATE INDEX idx_employee_status ON employees(status);
CREATE INDEX idx_employee_partner_id ON employees(partner_id);
CREATE INDEX idx_employee_supervisor_id ON employees(supervisor_id);

-- Comments for employees table
COMMENT ON TABLE employees IS 'Employee master data for KPI management system';
COMMENT ON COLUMN employees.employee_id IS 'Unique employee identifier';
COMMENT ON COLUMN employees.password IS 'Encrypted password for authentication';
COMMENT ON COLUMN employees.partner_id IS 'Reference to partner/company organization';
COMMENT ON COLUMN employees.supervisor_id IS 'Reference to supervisor employee ID';

-- =====================================================
-- Table: kpis
-- Description: Key Performance Indicators tracking
-- =====================================================
CREATE TABLE kpis (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL,
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
CREATE INDEX idx_kpi_employee_id ON kpis(employee_id);
CREATE INDEX idx_kpi_status ON kpis(status);
CREATE INDEX idx_kpi_deleted ON kpis(is_deleted);
CREATE INDEX idx_kpi_start_at ON kpis(start_at);
CREATE INDEX idx_kpi_end_at ON kpis(end_at);

-- Comments for kpis table
COMMENT ON TABLE kpis IS 'Main KPI tracking table';
COMMENT ON COLUMN kpis.employee_id IS 'Reference to employee ID';
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
    employee_id VARCHAR(50) NOT NULL,
    target_value INTEGER NOT NULL DEFAULT 0,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_kpi_parameter_kpi FOREIGN KEY (kpi_id) REFERENCES kpis(id) ON DELETE CASCADE,
    CONSTRAINT chk_kpi_parameter_target_value CHECK (target_value >= 0 AND target_value <= 100)
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
    is_completed BOOLEAN NOT NULL DEFAULT TRUE,
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
-- Table: audit_logs
-- Description: Audit trail for system actions
-- =====================================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(20) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    request_details TEXT,
    response_details TEXT,
    old_value TEXT,
    new_value TEXT,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comments for audit_logs table
COMMENT ON TABLE audit_logs IS 'Audit trail for tracking system actions and changes';
COMMENT ON COLUMN audit_logs.action_type IS 'Type of action performed';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity affected';
COMMENT ON COLUMN audit_logs.old_value IS 'Previous value as TEXT';
COMMENT ON COLUMN audit_logs.new_value IS 'New value as TEXT';

-- =====================================================
-- End of Migration V1
-- =====================================================
