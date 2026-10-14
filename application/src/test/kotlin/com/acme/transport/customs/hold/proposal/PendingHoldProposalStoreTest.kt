package com.acme.transport.customs.hold.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingHoldProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingHoldProposalStore()
        val item = newPendingHoldProposal("code-160", "name-129")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
