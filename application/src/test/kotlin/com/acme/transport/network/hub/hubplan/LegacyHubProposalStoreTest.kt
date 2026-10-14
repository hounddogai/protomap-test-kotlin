package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyHubProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyHubProposalStore()
        val item = newLegacyHubProposal("code-742", "label-771")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
