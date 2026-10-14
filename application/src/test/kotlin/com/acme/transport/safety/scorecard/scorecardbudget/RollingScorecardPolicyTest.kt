package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingScorecardPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingScorecardPolicy("code-291", "label-386")
        assertEquals(RollingScorecardPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingScorecardPolicy("code-291", "label-386")
        try {
            item.moveTo(RollingScorecardPolicyStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingScorecardPolicyStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingScorecardPolicy("code-291", "label-386")
        item.status = RollingScorecardPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingScorecardPolicyStatus.PLANNED) }
    }
}
