package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newSwapMetric("code-663", "title-639")
        assertEquals(SwapMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newSwapMetric("code-663", "title-639")
        try {
            item.moveTo(SwapMetricStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapMetricStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSwapMetric("code-663", "title-639")
        item.status = SwapMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SwapMetricStatus.PROPOSED) }
    }
}
