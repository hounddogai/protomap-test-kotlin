package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newFuelSurchargeTemplate("code-499", "label-678")
        assertEquals(FuelSurchargeTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFuelSurchargeTemplate("code-499", "label-678")
        try {
            item.moveTo(FuelSurchargeTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelSurchargeTemplate("code-499", "label-678")
        item.status = FuelSurchargeTemplateStatus.FAILED
        assertFails("move") { item.moveTo(FuelSurchargeTemplateStatus.NEW) }
    }
}
