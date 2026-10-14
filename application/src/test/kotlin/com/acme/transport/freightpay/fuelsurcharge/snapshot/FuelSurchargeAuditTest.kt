package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeAudit("code-761", "title-913")
        assertEquals(FuelSurchargeAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFuelSurchargeAudit("code-761", "title-913")
        try {
            item.moveTo(FuelSurchargeAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeAudit("code-761", "title-913")
        item.status = FuelSurchargeAuditStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeAuditStatus.IDLE) }
    }
}
