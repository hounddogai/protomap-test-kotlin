package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingProofOfDeliveryMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingProofOfDeliveryMapping("code-131", "title-935")
        assertEquals(RollingProofOfDeliveryMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRollingProofOfDeliveryMapping("code-131", "title-935")
        try {
            item.moveTo(RollingProofOfDeliveryMappingStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingProofOfDeliveryMappingStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingProofOfDeliveryMapping("code-131", "title-935")
        item.status = RollingProofOfDeliveryMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingProofOfDeliveryMappingStatus.PLANNED) }
    }
}
