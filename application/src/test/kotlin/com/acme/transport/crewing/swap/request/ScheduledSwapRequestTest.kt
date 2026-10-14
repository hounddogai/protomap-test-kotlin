package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSwapRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledSwapRequest("code-530", "name-148")
        assertEquals(ScheduledSwapRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledSwapRequest("code-530", "name-148")
        try {
            item.moveTo(ScheduledSwapRequestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSwapRequestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledSwapRequest("code-530", "name-148")
        item.status = ScheduledSwapRequestStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledSwapRequestStatus.IDLE) }
    }
}
