package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportProposalStore()
        val item = newCarbonReportProposal("code-237", "title-389")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
