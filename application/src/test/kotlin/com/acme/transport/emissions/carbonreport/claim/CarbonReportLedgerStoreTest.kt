package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportLedgerStore()
        val item = newCarbonReportLedger("code-535", "label-485")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
