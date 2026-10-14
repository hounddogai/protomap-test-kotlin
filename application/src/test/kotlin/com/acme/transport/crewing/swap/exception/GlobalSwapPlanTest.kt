package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalSwapPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalSwapPlan("code-237", "name-837")
        assertEquals(GlobalSwapPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalSwapPlan("code-237", "name-837")
        try {
            item.moveTo(GlobalSwapPlanStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalSwapPlanStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalSwapPlan("code-237", "name-837")
        item.status = GlobalSwapPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalSwapPlanStatus.REQUESTED) }
    }
}
