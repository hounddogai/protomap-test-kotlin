package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultProofOfDeliveryBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultProofOfDeliveryBundle("code-86", "name-595")
        assertEquals(DefaultProofOfDeliveryBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultProofOfDeliveryBundle("code-86", "name-595")
        try {
            item.moveTo(DefaultProofOfDeliveryBundleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultProofOfDeliveryBundleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultProofOfDeliveryBundle("code-86", "name-595")
        item.status = DefaultProofOfDeliveryBundleStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultProofOfDeliveryBundleStatus.IDLE) }
    }
}
