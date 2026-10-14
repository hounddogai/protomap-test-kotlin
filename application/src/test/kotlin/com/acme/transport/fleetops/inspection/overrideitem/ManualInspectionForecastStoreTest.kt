package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualInspectionForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualInspectionForecastStore()
        val item = newManualInspectionForecast("code-852", "title-94")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
