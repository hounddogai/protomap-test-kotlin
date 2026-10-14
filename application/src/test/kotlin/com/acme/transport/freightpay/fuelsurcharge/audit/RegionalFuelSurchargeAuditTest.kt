package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelSurchargeAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalFuelSurchargeAudit("code-17", "name-290")
        assertEquals(RegionalFuelSurchargeAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalFuelSurchargeAudit("code-17", "name-290")
        try {
            item.moveTo(RegionalFuelSurchargeAuditStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelSurchargeAuditStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalFuelSurchargeAudit("code-17", "name-290")
        item.status = RegionalFuelSurchargeAuditStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalFuelSurchargeAuditStatus.DRAFT) }
    }
}
