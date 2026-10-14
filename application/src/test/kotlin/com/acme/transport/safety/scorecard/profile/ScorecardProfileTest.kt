package com.acme.transport.safety.scorecard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardProfile("code-498", "label-9")
        assertEquals(ScorecardProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScorecardProfile("code-498", "label-9")
        try {
            item.moveTo(ScorecardProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardProfile("code-498", "label-9")
        item.status = ScorecardProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardProfileStatus.REQUESTED) }
    }
}
