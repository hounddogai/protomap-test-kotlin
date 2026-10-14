package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCarbonReportForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCarbonReportForecastStore()
        val item = newManualCarbonReportForecast("code-349", "label-70")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
