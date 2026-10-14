package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardAssignmentStore()
        val item = newScorecardAssignment("code-372", "label-711")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
