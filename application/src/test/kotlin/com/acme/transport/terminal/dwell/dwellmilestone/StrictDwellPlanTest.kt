package com.acme.transport.terminal.dwell.dwellmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDwellPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictDwellPlan("code-879", "label-663")
        assertEquals(StrictDwellPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newStrictDwellPlan("code-879", "label-663")
        try {
            item.moveTo(StrictDwellPlanStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDwellPlanStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictDwellPlan("code-879", "label-663")
        item.status = StrictDwellPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictDwellPlanStatus.PROPOSED) }
    }
}
