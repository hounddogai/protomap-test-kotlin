package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newLockerMilestone("code-172", "title-200")
        assertEquals(LockerMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLockerMilestone("code-172", "title-200")
        try {
            item.moveTo(LockerMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerMilestone("code-172", "title-200")
        item.status = LockerMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(LockerMilestoneStatus.CREATED) }
    }
}
