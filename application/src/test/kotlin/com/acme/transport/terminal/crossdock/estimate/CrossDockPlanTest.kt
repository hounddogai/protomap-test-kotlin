package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newCrossDockPlan("code-277", "title-973")
        assertEquals(CrossDockPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCrossDockPlan("code-277", "title-973")
        try {
            item.moveTo(CrossDockPlanStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockPlanStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCrossDockPlan("code-277", "title-973")
        item.status = CrossDockPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(CrossDockPlanStatus.PLANNED) }
    }
}
