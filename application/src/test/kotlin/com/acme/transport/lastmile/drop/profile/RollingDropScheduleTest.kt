package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDropScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingDropSchedule("code-194", "label-442")
        assertEquals(RollingDropScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingDropSchedule("code-194", "label-442")
        try {
            item.moveTo(RollingDropScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDropScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingDropSchedule("code-194", "label-442")
        item.status = RollingDropScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingDropScheduleStatus.REQUESTED) }
    }
}
