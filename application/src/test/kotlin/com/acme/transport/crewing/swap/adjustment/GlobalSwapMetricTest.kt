package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalSwapMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalSwapMetric("code-533", "title-933")
        assertEquals(GlobalSwapMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalSwapMetric("code-533", "title-933")
        try {
            item.moveTo(GlobalSwapMetricStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalSwapMetricStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalSwapMetric("code-533", "title-933")
        item.status = GlobalSwapMetricStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalSwapMetricStatus.IDLE) }
    }
}
