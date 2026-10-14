package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCarbonReportProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCarbonReportProposalStore()
        val item = newAdaptiveCarbonReportProposal("code-100", "title-567")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
