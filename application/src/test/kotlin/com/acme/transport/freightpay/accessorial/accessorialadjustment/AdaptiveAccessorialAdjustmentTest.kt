package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAccessorialAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveAccessorialAdjustment("code-990", "label-565")
        assertEquals(AdaptiveAccessorialAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveAccessorialAdjustment("code-990", "label-565")
        try {
            item.moveTo(AdaptiveAccessorialAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAccessorialAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveAccessorialAdjustment("code-990", "label-565")
        item.status = AdaptiveAccessorialAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveAccessorialAdjustmentStatus.REQUESTED) }
    }
}
