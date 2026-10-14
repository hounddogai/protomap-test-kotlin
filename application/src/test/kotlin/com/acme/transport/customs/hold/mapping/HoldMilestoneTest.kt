package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldMilestone("code-921", "name-155")
        assertEquals(HoldMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newHoldMilestone("code-921", "name-155")
        try {
            item.moveTo(HoldMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldMilestone("code-921", "name-155")
        item.status = HoldMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldMilestoneStatus.REQUESTED) }
    }
}
