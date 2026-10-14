package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultScorecardAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultScorecardAllocationStore()
        val item = newDefaultScorecardAllocation("code-153", "title-802")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
