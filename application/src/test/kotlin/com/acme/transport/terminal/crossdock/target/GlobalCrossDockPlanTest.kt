package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCrossDockPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalCrossDockPlan("code-704", "label-534")
        assertEquals(GlobalCrossDockPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalCrossDockPlan("code-704", "label-534")
        try {
            item.moveTo(GlobalCrossDockPlanStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCrossDockPlanStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalCrossDockPlan("code-704", "label-534")
        item.status = GlobalCrossDockPlanStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalCrossDockPlanStatus.DRAFT) }
    }
}
