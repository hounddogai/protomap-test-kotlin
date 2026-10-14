package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodAuditStore()
        val item = newDutyPeriodAudit("code-172", "name-587")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
