package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDutyPeriodAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDutyPeriodAdjustmentStore()
        val item = newCappedDutyPeriodAdjustment("code-263", "name-574")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
