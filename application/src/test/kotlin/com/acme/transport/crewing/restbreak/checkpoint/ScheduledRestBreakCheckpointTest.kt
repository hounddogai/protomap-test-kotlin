package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRestBreakCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledRestBreakCheckpoint("code-757", "name-822")
        assertEquals(ScheduledRestBreakCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newScheduledRestBreakCheckpoint("code-757", "name-822")
        try {
            item.moveTo(ScheduledRestBreakCheckpointStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRestBreakCheckpointStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledRestBreakCheckpoint("code-757", "name-822")
        item.status = ScheduledRestBreakCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledRestBreakCheckpointStatus.IDLE) }
    }
}
