package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalScorecardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalScorecardAssignmentStore()
        val item = newSeasonalScorecardAssignment("code-468", "label-6")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
