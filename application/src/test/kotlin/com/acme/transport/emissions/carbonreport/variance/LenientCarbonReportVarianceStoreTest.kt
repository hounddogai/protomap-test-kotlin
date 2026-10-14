package com.acme.transport.emissions.carbonreport.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCarbonReportVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCarbonReportVarianceStore()
        val item = newLenientCarbonReportVariance("code-404", "label-101")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
