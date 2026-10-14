package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodBaselineStore()
        val item = newDutyPeriodBaseline("code-146", "title-342")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
