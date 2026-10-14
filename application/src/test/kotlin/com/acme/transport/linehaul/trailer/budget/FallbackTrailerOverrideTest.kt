package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTrailerOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackTrailerOverride("code-955", "label-92")
        assertEquals(FallbackTrailerOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackTrailerOverride("code-955", "label-92")
        try {
            item.moveTo(FallbackTrailerOverrideStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTrailerOverrideStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackTrailerOverride("code-955", "label-92")
        item.status = FallbackTrailerOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackTrailerOverrideStatus.IDLE) }
    }
}
