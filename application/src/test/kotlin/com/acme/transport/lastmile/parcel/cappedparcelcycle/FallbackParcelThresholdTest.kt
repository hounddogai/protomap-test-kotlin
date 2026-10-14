package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackParcelThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackParcelThreshold("code-73", "name-197")
        assertEquals(FallbackParcelThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackParcelThreshold("code-73", "name-197")
        try {
            item.moveTo(FallbackParcelThresholdStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackParcelThresholdStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackParcelThreshold("code-73", "name-197")
        item.status = FallbackParcelThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackParcelThresholdStatus.DRAFT) }
    }
}
