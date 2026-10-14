package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRosterNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingRosterNotice("code-941", "name-555")
        assertEquals(PendingRosterNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingRosterNotice("code-941", "name-555")
        try {
            item.moveTo(PendingRosterNoticeStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRosterNoticeStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingRosterNotice("code-941", "name-555")
        item.status = PendingRosterNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingRosterNoticeStatus.PLANNED) }
    }
}
