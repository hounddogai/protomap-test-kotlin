package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffAdjustment("code-926", "label-389")
        assertEquals(CutoffAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCutoffAdjustment("code-926", "label-389")
        try {
            item.moveTo(CutoffAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffAdjustment("code-926", "label-389")
        item.status = CutoffAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(CutoffAdjustmentStatus.NEW) }
    }
}
