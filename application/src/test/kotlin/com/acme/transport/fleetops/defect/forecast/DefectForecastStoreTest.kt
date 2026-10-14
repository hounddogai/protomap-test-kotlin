package com.acme.transport.fleetops.defect.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectForecastStore()
        val item = newDefectForecast("code-573", "title-850")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
