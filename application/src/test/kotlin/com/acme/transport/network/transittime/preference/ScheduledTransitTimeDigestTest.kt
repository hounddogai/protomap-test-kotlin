package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTransitTimeDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledTransitTimeDigest("code-913", "title-73")
        assertEquals(ScheduledTransitTimeDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newScheduledTransitTimeDigest("code-913", "title-73")
        try {
            item.moveTo(ScheduledTransitTimeDigestStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTransitTimeDigestStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledTransitTimeDigest("code-913", "title-73")
        item.status = ScheduledTransitTimeDigestStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledTransitTimeDigestStatus.IDLE) }
    }
}
