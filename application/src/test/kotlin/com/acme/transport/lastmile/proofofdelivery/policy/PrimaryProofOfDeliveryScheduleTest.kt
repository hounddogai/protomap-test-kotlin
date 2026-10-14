package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryProofOfDeliveryScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryProofOfDeliverySchedule("code-825", "label-718")
        assertEquals(PrimaryProofOfDeliveryScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryProofOfDeliverySchedule("code-825", "label-718")
        try {
            item.moveTo(PrimaryProofOfDeliveryScheduleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryProofOfDeliveryScheduleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryProofOfDeliverySchedule("code-825", "label-718")
        item.status = PrimaryProofOfDeliveryScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryProofOfDeliveryScheduleStatus.REQUESTED) }
    }
}
