package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyProofOfDeliveryWindowTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyProofOfDeliveryWindow("code-446", "title-128")
        assertEquals(LegacyProofOfDeliveryWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLegacyProofOfDeliveryWindow("code-446", "title-128")
        try {
            item.moveTo(LegacyProofOfDeliveryWindowStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyProofOfDeliveryWindowStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyProofOfDeliveryWindow("code-446", "title-128")
        item.status = LegacyProofOfDeliveryWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyProofOfDeliveryWindowStatus.OPEN) }
    }
}
