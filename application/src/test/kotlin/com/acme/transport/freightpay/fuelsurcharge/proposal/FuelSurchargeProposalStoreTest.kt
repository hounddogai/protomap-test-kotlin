package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeProposalStore()
        val item = newFuelSurchargeProposal("code-771", "label-833")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
