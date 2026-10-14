package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerProposalStore()
        val item = newBrokerProposal("code-605", "title-440")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
