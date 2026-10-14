package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newProofOfDeliveryGroup("code-540", "name-69")
        assertEquals(ProofOfDeliveryGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newProofOfDeliveryGroup("code-540", "name-69")
        try {
            item.moveTo(ProofOfDeliveryGroupStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryGroupStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newProofOfDeliveryGroup("code-540", "name-69")
        item.status = ProofOfDeliveryGroupStatus.STOPPED
        assertFails("move") { item.moveTo(ProofOfDeliveryGroupStatus.IDLE) }
    }
}
