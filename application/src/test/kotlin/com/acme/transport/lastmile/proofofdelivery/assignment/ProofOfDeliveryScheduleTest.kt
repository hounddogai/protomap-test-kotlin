package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newProofOfDeliverySchedule("code-227", "name-715")
        assertEquals(ProofOfDeliveryScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newProofOfDeliverySchedule("code-227", "name-715")
        try {
            item.moveTo(ProofOfDeliveryScheduleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryScheduleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newProofOfDeliverySchedule("code-227", "name-715")
        item.status = ProofOfDeliveryScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(ProofOfDeliveryScheduleStatus.IDLE) }
    }
}
