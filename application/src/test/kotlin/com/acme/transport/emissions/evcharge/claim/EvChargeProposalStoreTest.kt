package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeProposalStore()
        val item = newEvChargeProposal("code-796", "label-477")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
