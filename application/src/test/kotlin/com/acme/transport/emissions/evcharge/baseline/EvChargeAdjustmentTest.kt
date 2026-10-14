package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeAdjustment("code-949", "title-930")
        assertEquals(EvChargeAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEvChargeAdjustment("code-949", "title-930")
        try {
            item.moveTo(EvChargeAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeAdjustment("code-949", "title-930")
        item.status = EvChargeAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeAdjustmentStatus.NEW) }
    }
}
