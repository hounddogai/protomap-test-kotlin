package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingSwapPlan("code-981", "name-594")
        assertEquals(RollingSwapPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingSwapPlan("code-981", "name-594")
        try {
            item.moveTo(RollingSwapPlanStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapPlanStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingSwapPlan("code-981", "name-594")
        item.status = RollingSwapPlanStatus.CLOSED
        assertFails("move") { item.moveTo(RollingSwapPlanStatus.DRAFT) }
    }
}
