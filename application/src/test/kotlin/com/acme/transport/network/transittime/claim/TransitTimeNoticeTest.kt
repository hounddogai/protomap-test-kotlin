package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newTransitTimeNotice("code-742", "title-945")
        assertEquals(TransitTimeNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTransitTimeNotice("code-742", "title-945")
        try {
            item.moveTo(TransitTimeNoticeStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeNoticeStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTransitTimeNotice("code-742", "title-945")
        item.status = TransitTimeNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(TransitTimeNoticeStatus.PLANNED) }
    }
}
