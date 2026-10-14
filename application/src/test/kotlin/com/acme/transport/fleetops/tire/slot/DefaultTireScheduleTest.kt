package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTireScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultTireSchedule("code-117", "name-275")
        assertEquals(DefaultTireScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultTireSchedule("code-117", "name-275")
        try {
            item.moveTo(DefaultTireScheduleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTireScheduleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultTireSchedule("code-117", "name-275")
        item.status = DefaultTireScheduleStatus.FAILED
        assertFails("move") { item.moveTo(DefaultTireScheduleStatus.NEW) }
    }
}
