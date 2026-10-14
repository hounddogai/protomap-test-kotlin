package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newSwapRule("code-831", "title-181")
        assertEquals(SwapRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newSwapRule("code-831", "title-181")
        try {
            item.moveTo(SwapRuleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapRuleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSwapRule("code-831", "title-181")
        item.status = SwapRuleStatus.STOPPED
        assertFails("move") { item.moveTo(SwapRuleStatus.IDLE) }
    }
}
