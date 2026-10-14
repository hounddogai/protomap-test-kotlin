package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCutoffScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultCutoffSchedule("code-773", "label-511")
        assertEquals(DefaultCutoffScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultCutoffSchedule("code-773", "label-511")
        try {
            item.moveTo(DefaultCutoffScheduleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCutoffScheduleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultCutoffSchedule("code-773", "label-511")
        item.status = DefaultCutoffScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultCutoffScheduleStatus.IDLE) }
    }
}
