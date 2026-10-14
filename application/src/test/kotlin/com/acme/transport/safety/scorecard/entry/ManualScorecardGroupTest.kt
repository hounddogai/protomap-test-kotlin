package com.acme.transport.safety.scorecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualScorecardGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newManualScorecardGroup("code-395", "name-138")
        assertEquals(ManualScorecardGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newManualScorecardGroup("code-395", "name-138")
        try {
            item.moveTo(ManualScorecardGroupStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualScorecardGroupStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualScorecardGroup("code-395", "name-138")
        item.status = ManualScorecardGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualScorecardGroupStatus.PLANNED) }
    }
}
