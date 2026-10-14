package com.acme.transport.fleetops.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionForecastStore()
        val item = newInspectionForecast("code-484", "name-629")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
