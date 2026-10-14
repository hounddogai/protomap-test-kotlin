package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCarbonReportLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCarbonReportLedgerStore()
        val item = newWeightedCarbonReportLedger("code-53", "name-547")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
