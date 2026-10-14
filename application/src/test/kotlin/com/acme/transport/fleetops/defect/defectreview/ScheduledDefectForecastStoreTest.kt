package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDefectForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDefectForecastStore()
        val item = newScheduledDefectForecast("code-822", "label-977")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
