package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRateCardScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingRateCardSchedule("code-332", "label-545")
        assertEquals(RollingRateCardScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRollingRateCardSchedule("code-332", "label-545")
        try {
            item.moveTo(RollingRateCardScheduleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRateCardScheduleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingRateCardSchedule("code-332", "label-545")
        item.status = RollingRateCardScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(RollingRateCardScheduleStatus.IDLE) }
    }
}
