package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newScorecardNotice("code-537", "label-118")
        assertEquals(ScorecardNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newScorecardNotice("code-537", "label-118")
        try {
            item.moveTo(ScorecardNoticeStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardNoticeStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScorecardNotice("code-537", "label-118")
        item.status = ScorecardNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(ScorecardNoticeStatus.IDLE) }
    }
}
