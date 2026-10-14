package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDutyPeriodEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDutyPeriodEstimateStore()
        val item = newRegionalDutyPeriodEstimate("code-389", "title-810")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
