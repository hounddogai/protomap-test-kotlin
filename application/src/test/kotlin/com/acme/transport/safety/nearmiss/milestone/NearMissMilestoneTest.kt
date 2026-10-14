package com.acme.transport.safety.nearmiss.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newNearMissMilestone("code-380", "label-187")
        assertEquals(NearMissMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newNearMissMilestone("code-380", "label-187")
        try {
            item.moveTo(NearMissMilestoneStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissMilestoneStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNearMissMilestone("code-380", "label-187")
        item.status = NearMissMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(NearMissMilestoneStatus.DRAFT) }
    }
}
