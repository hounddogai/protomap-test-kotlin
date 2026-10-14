package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDutyPeriodForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDutyPeriodForecastStore()
        val item = newBatchedDutyPeriodForecast("code-679", "name-882")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
