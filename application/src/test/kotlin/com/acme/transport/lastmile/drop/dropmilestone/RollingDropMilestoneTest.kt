package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDropMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingDropMilestone("code-974", "label-463")
        assertEquals(RollingDropMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRollingDropMilestone("code-974", "label-463")
        try {
            item.moveTo(RollingDropMilestoneStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDropMilestoneStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingDropMilestone("code-974", "label-463")
        item.status = RollingDropMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingDropMilestoneStatus.REQUESTED) }
    }
}
