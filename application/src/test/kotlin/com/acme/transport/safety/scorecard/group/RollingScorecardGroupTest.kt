package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingScorecardGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingScorecardGroup("code-509", "title-479")
        assertEquals(RollingScorecardGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRollingScorecardGroup("code-509", "title-479")
        try {
            item.moveTo(RollingScorecardGroupStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingScorecardGroupStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingScorecardGroup("code-509", "title-479")
        item.status = RollingScorecardGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingScorecardGroupStatus.REQUESTED) }
    }
}
