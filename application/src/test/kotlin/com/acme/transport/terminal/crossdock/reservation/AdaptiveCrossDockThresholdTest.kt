package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCrossDockThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveCrossDockThreshold("code-192", "label-109")
        assertEquals(AdaptiveCrossDockThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveCrossDockThreshold("code-192", "label-109")
        try {
            item.moveTo(AdaptiveCrossDockThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCrossDockThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveCrossDockThreshold("code-192", "label-109")
        item.status = AdaptiveCrossDockThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveCrossDockThresholdStatus.PROPOSED) }
    }
}
