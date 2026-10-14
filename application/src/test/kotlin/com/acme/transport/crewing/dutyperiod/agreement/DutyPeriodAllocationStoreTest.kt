package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodAllocationStore()
        val item = newDutyPeriodAllocation("code-216", "name-711")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
