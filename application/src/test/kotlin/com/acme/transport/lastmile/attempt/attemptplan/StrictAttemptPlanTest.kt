package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAttemptPlanTest {
    @Test
    fun `starts in created`() {
        val item = newStrictAttemptPlan("code-312", "title-159")
        assertEquals(StrictAttemptPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictAttemptPlan("code-312", "title-159")
        try {
            item.moveTo(StrictAttemptPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAttemptPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictAttemptPlan("code-312", "title-159")
        item.status = StrictAttemptPlanStatus.RETIRED
        assertFails("move") { item.moveTo(StrictAttemptPlanStatus.CREATED) }
    }
}
