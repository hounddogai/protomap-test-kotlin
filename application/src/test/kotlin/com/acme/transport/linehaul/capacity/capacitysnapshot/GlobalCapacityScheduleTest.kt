package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCapacityScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalCapacitySchedule("code-986", "label-81")
        assertEquals(GlobalCapacityScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalCapacitySchedule("code-986", "label-81")
        try {
            item.moveTo(GlobalCapacityScheduleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCapacityScheduleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalCapacitySchedule("code-986", "label-81")
        item.status = GlobalCapacityScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalCapacityScheduleStatus.PROPOSED) }
    }
}
