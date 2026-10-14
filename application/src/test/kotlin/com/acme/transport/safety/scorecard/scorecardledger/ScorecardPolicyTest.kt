package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardPolicy("code-228", "name-788")
        assertEquals(ScorecardPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScorecardPolicy("code-228", "name-788")
        try {
            item.moveTo(ScorecardPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardPolicy("code-228", "name-788")
        item.status = ScorecardPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardPolicyStatus.REQUESTED) }
    }
}
