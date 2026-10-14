package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportMetricStore()
        val item = newCarbonReportMetric("code-1", "label-244")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
