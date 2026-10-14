package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDutyPeriodEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDutyPeriodEstimateStore()
        val item = newStrictDutyPeriodEstimate("code-104", "name-926")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
