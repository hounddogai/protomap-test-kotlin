package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDutyPeriodAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDutyPeriodAllocationStore()
        val item = newWeightedDutyPeriodAllocation("code-225", "label-478")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
