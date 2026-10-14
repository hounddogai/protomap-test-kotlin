package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeSchedule("code-489", "label-945")
        assertEquals(NodeScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newNodeSchedule("code-489", "label-945")
        try {
            item.moveTo(NodeScheduleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeScheduleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeSchedule("code-489", "label-945")
        item.status = NodeScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeScheduleStatus.PLANNED) }
    }
}
