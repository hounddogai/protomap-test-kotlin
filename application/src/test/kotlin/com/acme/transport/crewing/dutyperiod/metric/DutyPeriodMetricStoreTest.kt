package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodMetricStore()
        val item = newDutyPeriodMetric("code-393", "title-185")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
