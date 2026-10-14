package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCarbonReportEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCarbonReportEstimateStore()
        val item = newStrictCarbonReportEstimate("code-444", "label-792")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
