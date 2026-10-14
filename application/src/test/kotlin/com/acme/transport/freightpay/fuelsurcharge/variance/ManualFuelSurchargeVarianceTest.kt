package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFuelSurchargeVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newManualFuelSurchargeVariance("code-802", "label-953")
        assertEquals(ManualFuelSurchargeVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualFuelSurchargeVariance("code-802", "label-953")
        try {
            item.moveTo(ManualFuelSurchargeVarianceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFuelSurchargeVarianceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualFuelSurchargeVariance("code-802", "label-953")
        item.status = ManualFuelSurchargeVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualFuelSurchargeVarianceStatus.REQUESTED) }
    }
}
