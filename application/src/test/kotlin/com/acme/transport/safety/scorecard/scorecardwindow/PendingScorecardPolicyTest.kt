package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingScorecardPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingScorecardPolicy("code-414", "title-175")
        assertEquals(PendingScorecardPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingScorecardPolicy("code-414", "title-175")
        try {
            item.moveTo(PendingScorecardPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingScorecardPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingScorecardPolicy("code-414", "title-175")
        item.status = PendingScorecardPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingScorecardPolicyStatus.PROPOSED) }
    }
}
