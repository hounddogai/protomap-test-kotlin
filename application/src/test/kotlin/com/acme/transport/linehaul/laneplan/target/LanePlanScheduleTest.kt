package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newLanePlanSchedule("code-567", "label-457")
        assertEquals(LanePlanScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLanePlanSchedule("code-567", "label-457")
        try {
            item.moveTo(LanePlanScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLanePlanSchedule("code-567", "label-457")
        item.status = LanePlanScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(LanePlanScheduleStatus.REQUESTED) }
    }
}
