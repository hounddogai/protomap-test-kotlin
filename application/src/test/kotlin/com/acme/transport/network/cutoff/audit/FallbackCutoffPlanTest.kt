package com.acme.transport.network.cutoff.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCutoffPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackCutoffPlan("code-5", "name-916")
        assertEquals(FallbackCutoffPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newFallbackCutoffPlan("code-5", "name-916")
        try {
            item.moveTo(FallbackCutoffPlanStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCutoffPlanStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackCutoffPlan("code-5", "name-916")
        item.status = FallbackCutoffPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackCutoffPlanStatus.REQUESTED) }
    }
}
