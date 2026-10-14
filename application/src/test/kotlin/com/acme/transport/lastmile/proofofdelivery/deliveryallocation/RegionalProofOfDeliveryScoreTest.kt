package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalProofOfDeliveryScoreTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalProofOfDeliveryScore("code-874", "label-562")
        assertEquals(RegionalProofOfDeliveryScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalProofOfDeliveryScore("code-874", "label-562")
        try {
            item.moveTo(RegionalProofOfDeliveryScoreStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalProofOfDeliveryScoreStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalProofOfDeliveryScore("code-874", "label-562")
        item.status = RegionalProofOfDeliveryScoreStatus.FAILED
        assertFails("move") { item.moveTo(RegionalProofOfDeliveryScoreStatus.NEW) }
    }
}
