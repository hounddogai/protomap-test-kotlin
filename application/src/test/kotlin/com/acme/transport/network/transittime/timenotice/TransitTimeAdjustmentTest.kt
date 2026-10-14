package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newTransitTimeAdjustment("code-522", "label-370")
        assertEquals(TransitTimeAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTransitTimeAdjustment("code-522", "label-370")
        try {
            item.moveTo(TransitTimeAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTransitTimeAdjustment("code-522", "label-370")
        item.status = TransitTimeAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(TransitTimeAdjustmentStatus.NEW) }
    }
}
