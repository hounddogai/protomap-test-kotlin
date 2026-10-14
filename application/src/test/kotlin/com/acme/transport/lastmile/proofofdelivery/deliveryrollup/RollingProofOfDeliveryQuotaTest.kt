package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingProofOfDeliveryQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingProofOfDeliveryQuota("code-460", "title-532")
        assertEquals(RollingProofOfDeliveryQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRollingProofOfDeliveryQuota("code-460", "title-532")
        try {
            item.moveTo(RollingProofOfDeliveryQuotaStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingProofOfDeliveryQuotaStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingProofOfDeliveryQuota("code-460", "title-532")
        item.status = RollingProofOfDeliveryQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(RollingProofOfDeliveryQuotaStatus.IDLE) }
    }
}
