package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardAllocationStore()
        val item = newScorecardAllocation("code-775", "title-557")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
