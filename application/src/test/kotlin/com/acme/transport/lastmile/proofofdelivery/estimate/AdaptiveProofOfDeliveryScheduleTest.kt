package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveProofOfDeliveryScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveProofOfDeliverySchedule("code-212", "name-735")
        assertEquals(AdaptiveProofOfDeliveryScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveProofOfDeliverySchedule("code-212", "name-735")
        try {
            item.moveTo(AdaptiveProofOfDeliveryScheduleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveProofOfDeliveryScheduleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveProofOfDeliverySchedule("code-212", "name-735")
        item.status = AdaptiveProofOfDeliveryScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveProofOfDeliveryScheduleStatus.PLANNED) }
    }
}
