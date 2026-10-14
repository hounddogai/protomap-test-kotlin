package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newSwapCycle("code-56", "label-914")
        assertEquals(SwapCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSwapCycle("code-56", "label-914")
        try {
            item.moveTo(SwapCycleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapCycleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSwapCycle("code-56", "label-914")
        item.status = SwapCycleStatus.CLOSED
        assertFails("move") { item.moveTo(SwapCycleStatus.DRAFT) }
    }
}
