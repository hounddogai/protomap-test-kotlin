package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCutoffAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredCutoffAdjustment("code-504", "label-687")
        assertEquals(TieredCutoffAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTieredCutoffAdjustment("code-504", "label-687")
        try {
            item.moveTo(TieredCutoffAdjustmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCutoffAdjustmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredCutoffAdjustment("code-504", "label-687")
        item.status = TieredCutoffAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(TieredCutoffAdjustmentStatus.IDLE) }
    }
}
