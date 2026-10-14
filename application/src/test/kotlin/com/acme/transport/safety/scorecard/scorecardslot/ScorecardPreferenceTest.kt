package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardPreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newScorecardPreference("code-456", "title-363")
        assertEquals(ScorecardPreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScorecardPreference("code-456", "title-363")
        try {
            item.moveTo(ScorecardPreferenceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardPreferenceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScorecardPreference("code-456", "title-363")
        item.status = ScorecardPreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(ScorecardPreferenceStatus.CREATED) }
    }
}
