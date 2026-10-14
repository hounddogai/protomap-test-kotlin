package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardSummaryStore()
        val item = newScorecardSummary("code-580", "name-967")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
