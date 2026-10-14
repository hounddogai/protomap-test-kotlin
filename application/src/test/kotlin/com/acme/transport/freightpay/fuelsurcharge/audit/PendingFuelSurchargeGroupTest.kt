package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFuelSurchargeGroupTest {
    @Test
    fun `starts in new`() {
        val item = newPendingFuelSurchargeGroup("code-287", "label-409")
        assertEquals(PendingFuelSurchargeGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPendingFuelSurchargeGroup("code-287", "label-409")
        try {
            item.moveTo(PendingFuelSurchargeGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFuelSurchargeGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingFuelSurchargeGroup("code-287", "label-409")
        item.status = PendingFuelSurchargeGroupStatus.FAILED
        assertFails("move") { item.moveTo(PendingFuelSurchargeGroupStatus.NEW) }
    }
}
