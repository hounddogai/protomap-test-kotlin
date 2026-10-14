package com.acme.transport.freightpay.fuelsurcharge.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFuelSurchargeVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingFuelSurchargeVariance("code-878", "title-168")
        assertEquals(PendingFuelSurchargeVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingFuelSurchargeVariance("code-878", "title-168")
        try {
            item.moveTo(PendingFuelSurchargeVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFuelSurchargeVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingFuelSurchargeVariance("code-878", "title-168")
        item.status = PendingFuelSurchargeVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingFuelSurchargeVarianceStatus.PLANNED) }
    }
}
