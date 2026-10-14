package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionForecastStore()
        val item = newInspectionForecast("code-610", "title-714")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
