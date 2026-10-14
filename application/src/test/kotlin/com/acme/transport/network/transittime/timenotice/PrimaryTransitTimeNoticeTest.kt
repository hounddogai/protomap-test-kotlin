package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTransitTimeNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryTransitTimeNotice("code-629", "label-21")
        assertEquals(PrimaryTransitTimeNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPrimaryTransitTimeNotice("code-629", "label-21")
        try {
            item.moveTo(PrimaryTransitTimeNoticeStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTransitTimeNoticeStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryTransitTimeNotice("code-629", "label-21")
        item.status = PrimaryTransitTimeNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryTransitTimeNoticeStatus.PLANNED) }
    }
}
