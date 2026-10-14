package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledClearanceBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledClearanceBaseline("code-189", "title-951")
        assertEquals(ScheduledClearanceBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledClearanceBaseline("code-189", "title-951")
        try {
            item.moveTo(ScheduledClearanceBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledClearanceBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledClearanceBaseline("code-189", "title-951")
        item.status = ScheduledClearanceBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledClearanceBaselineStatus.DRAFT) }
    }
}
