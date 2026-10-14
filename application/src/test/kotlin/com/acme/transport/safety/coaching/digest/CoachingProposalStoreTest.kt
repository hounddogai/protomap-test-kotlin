package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingProposalStore()
        val item = newCoachingProposal("code-617", "name-546")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
