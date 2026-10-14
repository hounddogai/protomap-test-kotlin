package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardOverride("code-476", "title-814")
        assertEquals(ScorecardOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newScorecardOverride("code-476", "title-814")
        try {
            item.moveTo(ScorecardOverrideStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardOverrideStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardOverride("code-476", "title-814")
        item.status = ScorecardOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardOverrideStatus.REQUESTED) }
    }
}
