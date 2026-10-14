package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualProofOfDeliveryPlanTest {
    @Test
    fun `starts in new`() {
        val item = newManualProofOfDeliveryPlan("code-35", "title-688")
        assertEquals(ManualProofOfDeliveryPlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualProofOfDeliveryPlan("code-35", "title-688")
        try {
            item.moveTo(ManualProofOfDeliveryPlanStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualProofOfDeliveryPlanStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualProofOfDeliveryPlan("code-35", "title-688")
        item.status = ManualProofOfDeliveryPlanStatus.FAILED
        assertFails("move") { item.moveTo(ManualProofOfDeliveryPlanStatus.NEW) }
    }
}
