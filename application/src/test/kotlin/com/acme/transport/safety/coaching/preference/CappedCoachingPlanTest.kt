package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCoachingPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedCoachingPlan("code-451", "title-590")
        assertEquals(CappedCoachingPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedCoachingPlan("code-451", "title-590")
        try {
            item.moveTo(CappedCoachingPlanStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCoachingPlanStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedCoachingPlan("code-451", "title-590")
        item.status = CappedCoachingPlanStatus.CLOSED
        assertFails("move") { item.moveTo(CappedCoachingPlanStatus.DRAFT) }
    }
}
