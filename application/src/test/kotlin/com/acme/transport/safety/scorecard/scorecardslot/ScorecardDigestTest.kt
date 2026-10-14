package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardDigest("code-829", "label-947")
        assertEquals(ScorecardDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScorecardDigest("code-829", "label-947")
        try {
            item.moveTo(ScorecardDigestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardDigestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardDigest("code-829", "label-947")
        item.status = ScorecardDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardDigestStatus.REQUESTED) }
    }
}
