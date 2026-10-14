package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newEvChargeSchedule("code-405", "label-827")
        assertEquals(EvChargeScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newEvChargeSchedule("code-405", "label-827")
        try {
            item.moveTo(EvChargeScheduleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeScheduleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEvChargeSchedule("code-405", "label-827")
        item.status = EvChargeScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EvChargeScheduleStatus.PROPOSED) }
    }
}
