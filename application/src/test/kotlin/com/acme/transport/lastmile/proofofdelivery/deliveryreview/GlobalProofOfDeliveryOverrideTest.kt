package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalProofOfDeliveryOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalProofOfDeliveryOverride("code-291", "name-427")
        assertEquals(GlobalProofOfDeliveryOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalProofOfDeliveryOverride("code-291", "name-427")
        try {
            item.moveTo(GlobalProofOfDeliveryOverrideStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalProofOfDeliveryOverrideStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalProofOfDeliveryOverride("code-291", "name-427")
        item.status = GlobalProofOfDeliveryOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalProofOfDeliveryOverrideStatus.IDLE) }
    }
}
