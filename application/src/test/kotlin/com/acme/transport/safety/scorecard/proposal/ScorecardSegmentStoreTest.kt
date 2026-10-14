package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardSegmentStore()
        val item = newScorecardSegment("code-744", "label-179")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
