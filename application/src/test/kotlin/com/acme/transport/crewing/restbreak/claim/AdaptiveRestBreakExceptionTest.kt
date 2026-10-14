package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRestBreakExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveRestBreakException("code-149", "label-422")
        assertEquals(AdaptiveRestBreakExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveRestBreakException("code-149", "label-422")
        try {
            item.moveTo(AdaptiveRestBreakExceptionStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRestBreakExceptionStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveRestBreakException("code-149", "label-422")
        item.status = AdaptiveRestBreakExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveRestBreakExceptionStatus.IDLE) }
    }
}
