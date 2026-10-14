package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newRateCardAssignment("code-583", "title-750")
        assertEquals(RateCardAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRateCardAssignment("code-583", "title-750")
        try {
            item.moveTo(RateCardAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRateCardAssignment("code-583", "title-750")
        item.status = RateCardAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(RateCardAssignmentStatus.IDLE) }
    }
}
