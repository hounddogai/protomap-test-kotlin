package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newDropSchedule("code-406", "name-366")
        assertEquals(DropScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDropSchedule("code-406", "name-366")
        try {
            item.moveTo(DropScheduleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropScheduleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDropSchedule("code-406", "name-366")
        item.status = DropScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(DropScheduleStatus.OPEN) }
    }
}
