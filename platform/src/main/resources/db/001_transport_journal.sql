CREATE TABLE IF NOT EXISTS transport_journal (
    journal_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id text NOT NULL,
    domain_name text NOT NULL,
    operation_name text NOT NULL,
    payload bytea NOT NULL,
    recorded_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS transport_journal_tenant ON transport_journal (tenant_id, domain_name);
