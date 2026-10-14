package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCoachingProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCoachingProposalStore()
        val item = newTieredCoachingProposal("code-78", "name-55")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
