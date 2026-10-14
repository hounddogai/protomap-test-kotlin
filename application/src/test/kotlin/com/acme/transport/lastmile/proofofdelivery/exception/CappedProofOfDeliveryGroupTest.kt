package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedProofOfDeliveryGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedProofOfDeliveryGroup("code-247", "label-933")
        assertEquals(CappedProofOfDeliveryGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCappedProofOfDeliveryGroup("code-247", "label-933")
        try {
            item.moveTo(CappedProofOfDeliveryGroupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedProofOfDeliveryGroupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedProofOfDeliveryGroup("code-247", "label-933")
        item.status = CappedProofOfDeliveryGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedProofOfDeliveryGroupStatus.REQUESTED) }
    }
}
