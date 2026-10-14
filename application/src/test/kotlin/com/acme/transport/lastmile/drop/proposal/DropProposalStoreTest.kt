package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropProposalStore()
        val item = newDropProposal("code-753", "label-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
