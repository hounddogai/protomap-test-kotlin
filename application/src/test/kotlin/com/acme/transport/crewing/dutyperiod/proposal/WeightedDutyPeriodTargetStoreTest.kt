package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDutyPeriodTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDutyPeriodTargetStore()
        val item = newWeightedDutyPeriodTarget("code-82", "name-179")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
