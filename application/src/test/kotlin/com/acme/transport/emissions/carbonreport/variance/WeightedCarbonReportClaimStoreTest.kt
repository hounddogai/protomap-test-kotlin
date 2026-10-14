package com.acme.transport.emissions.carbonreport.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCarbonReportClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCarbonReportClaimStore()
        val item = newWeightedCarbonReportClaim("code-689", "name-744")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
