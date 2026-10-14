package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingScorecardNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingScorecardNotice("code-70", "title-425")
        assertEquals(RollingScorecardNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingScorecardNotice("code-70", "title-425")
        try {
            item.moveTo(RollingScorecardNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingScorecardNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingScorecardNotice("code-70", "title-425")
        item.status = RollingScorecardNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(RollingScorecardNoticeStatus.IDLE) }
    }
}
