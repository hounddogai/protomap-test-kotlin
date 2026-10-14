package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredScorecardNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredScorecardNotice("code-236", "title-931")
        assertEquals(TieredScorecardNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTieredScorecardNotice("code-236", "title-931")
        try {
            item.moveTo(TieredScorecardNoticeStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredScorecardNoticeStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredScorecardNotice("code-236", "title-931")
        item.status = TieredScorecardNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredScorecardNoticeStatus.PLANNED) }
    }
}
