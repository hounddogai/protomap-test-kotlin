package com.acme.transport.lastmile.redelivery.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryProposalStore()
        val item = newRedeliveryProposal("code-880", "name-370")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
