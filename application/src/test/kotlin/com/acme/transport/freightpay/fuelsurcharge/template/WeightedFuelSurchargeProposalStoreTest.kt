package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedFuelSurchargeProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedFuelSurchargeProposalStore()
        val item = newWeightedFuelSurchargeProposal("code-753", "name-671")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
