package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newRestBreakPlan("code-498", "name-538")
        assertEquals(RestBreakPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRestBreakPlan("code-498", "name-538")
        try {
            item.moveTo(RestBreakPlanStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakPlanStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRestBreakPlan("code-498", "name-538")
        item.status = RestBreakPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(RestBreakPlanStatus.PLANNED) }
    }
}
