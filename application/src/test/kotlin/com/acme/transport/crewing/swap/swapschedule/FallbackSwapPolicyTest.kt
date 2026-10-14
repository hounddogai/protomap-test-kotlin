package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackSwapPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackSwapPolicy("code-82", "label-886")
        assertEquals(FallbackSwapPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackSwapPolicy("code-82", "label-886")
        try {
            item.moveTo(FallbackSwapPolicyStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackSwapPolicyStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackSwapPolicy("code-82", "label-886")
        item.status = FallbackSwapPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackSwapPolicyStatus.IDLE) }
    }
}
