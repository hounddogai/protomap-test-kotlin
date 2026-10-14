package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakProposalStore()
        val item = newRestBreakProposal("code-122", "label-989")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
