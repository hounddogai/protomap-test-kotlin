package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryRollupTest {
    @Test
    fun `starts in open`() {
        val item = newProofOfDeliveryRollup("code-556", "title-57")
        assertEquals(ProofOfDeliveryRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newProofOfDeliveryRollup("code-556", "title-57")
        try {
            item.moveTo(ProofOfDeliveryRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newProofOfDeliveryRollup("code-556", "title-57")
        item.status = ProofOfDeliveryRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(ProofOfDeliveryRollupStatus.OPEN) }
    }
}
