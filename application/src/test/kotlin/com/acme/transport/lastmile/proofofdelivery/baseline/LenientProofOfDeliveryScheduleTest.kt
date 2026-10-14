package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientProofOfDeliveryScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientProofOfDeliverySchedule("code-757", "label-389")
        assertEquals(LenientProofOfDeliveryScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLenientProofOfDeliverySchedule("code-757", "label-389")
        try {
            item.moveTo(LenientProofOfDeliveryScheduleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientProofOfDeliveryScheduleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientProofOfDeliverySchedule("code-757", "label-389")
        item.status = LenientProofOfDeliveryScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientProofOfDeliveryScheduleStatus.PLANNED) }
    }
}
