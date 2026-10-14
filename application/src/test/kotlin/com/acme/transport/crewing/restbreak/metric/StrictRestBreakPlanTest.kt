package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRestBreakPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictRestBreakPlan("code-512", "title-1")
        assertEquals(StrictRestBreakPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictRestBreakPlan("code-512", "title-1")
        try {
            item.moveTo(StrictRestBreakPlanStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRestBreakPlanStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictRestBreakPlan("code-512", "title-1")
        item.status = StrictRestBreakPlanStatus.CLOSED
        assertFails("move") { item.moveTo(StrictRestBreakPlanStatus.DRAFT) }
    }
}
