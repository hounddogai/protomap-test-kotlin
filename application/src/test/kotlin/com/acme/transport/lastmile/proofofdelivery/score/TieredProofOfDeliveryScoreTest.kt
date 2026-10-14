package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredProofOfDeliveryScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredProofOfDeliveryScore("code-223", "name-11")
        assertEquals(TieredProofOfDeliveryScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredProofOfDeliveryScore("code-223", "name-11")
        try {
            item.moveTo(TieredProofOfDeliveryScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredProofOfDeliveryScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredProofOfDeliveryScore("code-223", "name-11")
        item.status = TieredProofOfDeliveryScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredProofOfDeliveryScoreStatus.PROPOSED) }
    }
}
