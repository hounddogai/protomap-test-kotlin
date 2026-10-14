package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedScorecardNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newCappedScorecardNotice("code-950", "label-835")
        assertEquals(CappedScorecardNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedScorecardNotice("code-950", "label-835")
        try {
            item.moveTo(CappedScorecardNoticeStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedScorecardNoticeStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedScorecardNotice("code-950", "label-835")
        item.status = CappedScorecardNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedScorecardNoticeStatus.OPEN) }
    }
}
