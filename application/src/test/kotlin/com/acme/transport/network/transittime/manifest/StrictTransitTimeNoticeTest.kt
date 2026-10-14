package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTransitTimeNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictTransitTimeNotice("code-890", "title-738")
        assertEquals(StrictTransitTimeNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictTransitTimeNotice("code-890", "title-738")
        try {
            item.moveTo(StrictTransitTimeNoticeStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTransitTimeNoticeStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictTransitTimeNotice("code-890", "title-738")
        item.status = StrictTransitTimeNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictTransitTimeNoticeStatus.PLANNED) }
    }
}
