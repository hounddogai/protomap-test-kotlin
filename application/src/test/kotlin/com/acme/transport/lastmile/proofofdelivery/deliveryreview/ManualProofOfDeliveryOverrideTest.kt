package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualProofOfDeliveryOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newManualProofOfDeliveryOverride("code-613", "name-343")
        assertEquals(ManualProofOfDeliveryOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newManualProofOfDeliveryOverride("code-613", "name-343")
        try {
            item.moveTo(ManualProofOfDeliveryOverrideStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualProofOfDeliveryOverrideStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualProofOfDeliveryOverride("code-613", "name-343")
        item.status = ManualProofOfDeliveryOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(ManualProofOfDeliveryOverrideStatus.IDLE) }
    }
}
