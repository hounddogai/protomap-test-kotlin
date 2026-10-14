package com.acme.transport.crewing.roster.regionalrostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRosterAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedRosterAssignment("code-662", "title-976")
        assertEquals(WeightedRosterAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newWeightedRosterAssignment("code-662", "title-976")
        try {
            item.moveTo(WeightedRosterAssignmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRosterAssignmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedRosterAssignment("code-662", "title-976")
        item.status = WeightedRosterAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedRosterAssignmentStatus.CREATED) }
    }
}
