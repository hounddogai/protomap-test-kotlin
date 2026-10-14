package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodRollupStore()
        val item = newDutyPeriodRollup("code-386", "label-953")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
