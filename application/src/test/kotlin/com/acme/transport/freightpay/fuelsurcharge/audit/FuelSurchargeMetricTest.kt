package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeMetricTest {
    @Test
    fun `starts in new`() {
        val item = newFuelSurchargeMetric("code-760", "title-365")
        assertEquals(FuelSurchargeMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFuelSurchargeMetric("code-760", "title-365")
        try {
            item.moveTo(FuelSurchargeMetricStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeMetricStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelSurchargeMetric("code-760", "title-365")
        item.status = FuelSurchargeMetricStatus.FAILED
        assertFails("move") { item.moveTo(FuelSurchargeMetricStatus.NEW) }
    }
}
