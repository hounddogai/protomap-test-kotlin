package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalRedeliveryAssignment("code-270", "name-39")
        assertEquals(SeasonalRedeliveryAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalRedeliveryAssignment("code-270", "name-39")
        try {
            item.moveTo(SeasonalRedeliveryAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalRedeliveryAssignment("code-270", "name-39")
        item.status = SeasonalRedeliveryAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalRedeliveryAssignmentStatus.CREATED) }
    }
}
