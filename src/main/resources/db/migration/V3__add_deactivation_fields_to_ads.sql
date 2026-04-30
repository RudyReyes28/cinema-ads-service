ALTER TABLE ads
    ADD COLUMN deactivated_by UUID,
    ADD COLUMN deactivated_at TIMESTAMP