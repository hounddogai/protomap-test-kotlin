package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultProofOfDeliveryMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultProofOfDeliveryMilestone("code-705", "name-43")
        assertEquals(DefaultProofOfDeliveryMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultProofOfDeliveryMilestone("code-705", "name-43")
        try {
            item.moveTo(DefaultProofOfDeliveryMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultProofOfDeliveryMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultProofOfDeliveryMilestone("code-705", "name-43")
        item.status = DefaultProofOfDeliveryMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultProofOfDeliveryMilestoneStatus.IDLE) }
    }
}
