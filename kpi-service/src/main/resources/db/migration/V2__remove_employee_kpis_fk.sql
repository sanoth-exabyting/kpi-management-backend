-- =====================================================
-- Migration: Remove FK constraint on employee_kpis.employee_id
-- Reason: employee_id references secondary DB, not primary employees table
-- =====================================================

-- Drop the foreign key constraint
ALTER TABLE employee_kpis 
DROP CONSTRAINT IF EXISTS fk_employee_kpis_employee;

-- Comment on the change
COMMENT ON COLUMN employee_kpis.employee_id IS 'Employee ID from secondary database - no FK constraint to primary employees table';
