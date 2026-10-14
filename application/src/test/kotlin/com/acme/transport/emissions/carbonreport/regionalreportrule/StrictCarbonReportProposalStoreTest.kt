package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCarbonReportProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCarbonReportProposalStore()
        val item = newStrictCarbonReportProposal("code-770", "name-384")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
