package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredProofOfDeliveryRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredProofOfDeliveryRollup("code-907", "name-56")
        assertEquals(TieredProofOfDeliveryRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredProofOfDeliveryRollup("code-907", "name-56")
        try {
            item.moveTo(TieredProofOfDeliveryRollupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredProofOfDeliveryRollupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredProofOfDeliveryRollup("code-907", "name-56")
        item.status = TieredProofOfDeliveryRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredProofOfDeliveryRollupStatus.PLANNED) }
    }
}
