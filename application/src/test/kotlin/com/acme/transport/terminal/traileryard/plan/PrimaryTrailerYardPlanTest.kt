package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTrailerYardPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryTrailerYardPlan("code-976", "title-301")
        assertEquals(PrimaryTrailerYardPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryTrailerYardPlan("code-976", "title-301")
        try {
            item.moveTo(PrimaryTrailerYardPlanStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTrailerYardPlanStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryTrailerYardPlan("code-976", "title-301")
        item.status = PrimaryTrailerYardPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryTrailerYardPlanStatus.REQUESTED) }
    }
}
