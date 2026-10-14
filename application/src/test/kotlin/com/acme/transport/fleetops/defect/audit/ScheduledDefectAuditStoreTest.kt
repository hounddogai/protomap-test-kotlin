package com.acme.transport.fleetops.defect.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDefectAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDefectAuditStore()
        val item = newScheduledDefectAudit("code-581", "label-908")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
