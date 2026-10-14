package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNodeProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledNodeProfile("code-402", "label-291")
        assertEquals(ScheduledNodeProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newScheduledNodeProfile("code-402", "label-291")
        try {
            item.moveTo(ScheduledNodeProfileStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNodeProfileStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledNodeProfile("code-402", "label-291")
        item.status = ScheduledNodeProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledNodeProfileStatus.PLANNED) }
    }
}
