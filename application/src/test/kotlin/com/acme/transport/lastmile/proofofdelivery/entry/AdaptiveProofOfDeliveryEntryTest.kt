package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveProofOfDeliveryEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveProofOfDeliveryEntry("code-877", "title-679")
        assertEquals(AdaptiveProofOfDeliveryEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveProofOfDeliveryEntry("code-877", "title-679")
        try {
            item.moveTo(AdaptiveProofOfDeliveryEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveProofOfDeliveryEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveProofOfDeliveryEntry("code-877", "title-679")
        item.status = AdaptiveProofOfDeliveryEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveProofOfDeliveryEntryStatus.REQUESTED) }
    }
}
