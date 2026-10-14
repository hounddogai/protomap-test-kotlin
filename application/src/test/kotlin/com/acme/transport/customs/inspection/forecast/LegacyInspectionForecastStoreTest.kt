package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionForecastStore()
        val item = newLegacyInspectionForecast("code-977", "label-864")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
