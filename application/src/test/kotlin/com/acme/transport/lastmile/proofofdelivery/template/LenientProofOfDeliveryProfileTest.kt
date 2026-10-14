package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientProofOfDeliveryProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientProofOfDeliveryProfile("code-790", "title-477")
        assertEquals(LenientProofOfDeliveryProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLenientProofOfDeliveryProfile("code-790", "title-477")
        try {
            item.moveTo(LenientProofOfDeliveryProfileStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientProofOfDeliveryProfileStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientProofOfDeliveryProfile("code-790", "title-477")
        item.status = LenientProofOfDeliveryProfileStatus.STOPPED
        assertFails("move") { item.moveTo(LenientProofOfDeliveryProfileStatus.IDLE) }
    }
}
