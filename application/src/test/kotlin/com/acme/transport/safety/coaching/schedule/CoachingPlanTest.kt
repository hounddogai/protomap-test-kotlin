package com.acme.transport.safety.coaching.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingPlanTest {
    @Test
    fun `starts in created`() {
        val item = newCoachingPlan("code-846", "name-160")
        assertEquals(CoachingPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCoachingPlan("code-846", "name-160")
        try {
            item.moveTo(CoachingPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCoachingPlan("code-846", "name-160")
        item.status = CoachingPlanStatus.RETIRED
        assertFails("move") { item.moveTo(CoachingPlanStatus.CREATED) }
    }
}
