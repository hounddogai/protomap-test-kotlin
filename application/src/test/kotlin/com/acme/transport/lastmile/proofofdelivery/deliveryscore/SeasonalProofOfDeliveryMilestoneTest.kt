package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalProofOfDeliveryMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalProofOfDeliveryMilestone("code-804", "name-705")
        assertEquals(SeasonalProofOfDeliveryMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalProofOfDeliveryMilestone("code-804", "name-705")
        try {
            item.moveTo(SeasonalProofOfDeliveryMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalProofOfDeliveryMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalProofOfDeliveryMilestone("code-804", "name-705")
        item.status = SeasonalProofOfDeliveryMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalProofOfDeliveryMilestoneStatus.NEW) }
    }
}
