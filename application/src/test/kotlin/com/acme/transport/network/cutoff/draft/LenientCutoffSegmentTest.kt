package com.acme.transport.network.cutoff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCutoffSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientCutoffSegment("code-264", "label-23")
        assertEquals(LenientCutoffSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLenientCutoffSegment("code-264", "label-23")
        try {
            item.moveTo(LenientCutoffSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCutoffSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientCutoffSegment("code-264", "label-23")
        item.status = LenientCutoffSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(LenientCutoffSegmentStatus.IDLE) }
    }
}
