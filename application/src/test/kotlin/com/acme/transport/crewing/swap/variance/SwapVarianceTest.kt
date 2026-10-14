package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newSwapVariance("code-962", "title-775")
        assertEquals(SwapVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newSwapVariance("code-962", "title-775")
        try {
            item.moveTo(SwapVarianceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapVarianceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSwapVariance("code-962", "title-775")
        item.status = SwapVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(SwapVarianceStatus.PLANNED) }
    }
}
