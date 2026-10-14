package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultProofOfDeliveryScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultProofOfDeliveryScore("code-157", "title-545")
        assertEquals(DefaultProofOfDeliveryScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newDefaultProofOfDeliveryScore("code-157", "title-545")
        try {
            item.moveTo(DefaultProofOfDeliveryScoreStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultProofOfDeliveryScoreStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultProofOfDeliveryScore("code-157", "title-545")
        item.status = DefaultProofOfDeliveryScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultProofOfDeliveryScoreStatus.PLANNED) }
    }
}
