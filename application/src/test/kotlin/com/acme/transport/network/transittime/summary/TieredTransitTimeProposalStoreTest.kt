package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTransitTimeProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTransitTimeProposalStore()
        val item = newTieredTransitTimeProposal("code-405", "label-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
