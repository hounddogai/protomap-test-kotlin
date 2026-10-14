package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRosterProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRosterProposalStore()
        val item = newGlobalRosterProposal("code-722", "name-901")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
