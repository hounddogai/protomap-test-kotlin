package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newSwapThreshold("code-22", "title-732")
        assertEquals(SwapThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSwapThreshold("code-22", "title-732")
        try {
            item.moveTo(SwapThresholdStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapThresholdStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSwapThreshold("code-22", "title-732")
        item.status = SwapThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(SwapThresholdStatus.PLANNED) }
    }
}
