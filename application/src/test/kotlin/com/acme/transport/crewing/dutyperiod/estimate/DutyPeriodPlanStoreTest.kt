package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodPlanStore()
        val item = newDutyPeriodPlan("code-485", "label-745")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
