package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFuelSurchargeScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newManualFuelSurchargeSchedule("code-511", "name-15")
        assertEquals(ManualFuelSurchargeScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualFuelSurchargeSchedule("code-511", "name-15")
        try {
            item.moveTo(ManualFuelSurchargeScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFuelSurchargeScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualFuelSurchargeSchedule("code-511", "name-15")
        item.status = ManualFuelSurchargeScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(ManualFuelSurchargeScheduleStatus.DRAFT) }
    }
}
