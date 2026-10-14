package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalFreightInvoiceProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalFreightInvoiceProposalStore()
        val item = newSeasonalFreightInvoiceProposal("code-249", "label-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
