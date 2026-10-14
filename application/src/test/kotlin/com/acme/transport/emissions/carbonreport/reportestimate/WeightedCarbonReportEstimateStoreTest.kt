package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCarbonReportEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCarbonReportEstimateStore()
        val item = newWeightedCarbonReportEstimate("code-603", "name-769")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
