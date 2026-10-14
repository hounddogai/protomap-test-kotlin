package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryHoldNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryHoldNotice("code-813", "name-235")
        assertEquals(PrimaryHoldNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPrimaryHoldNotice("code-813", "name-235")
        try {
            item.moveTo(PrimaryHoldNoticeStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryHoldNoticeStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryHoldNotice("code-813", "name-235")
        item.status = PrimaryHoldNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryHoldNoticeStatus.PLANNED) }
    }
}
