package com.acme.transport.freightpay.accessorial.accessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newAccessorialMilestone("code-459", "name-676")
        assertEquals(AccessorialMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAccessorialMilestone("code-459", "name-676")
        try {
            item.moveTo(AccessorialMilestoneStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialMilestoneStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAccessorialMilestone("code-459", "name-676")
        item.status = AccessorialMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AccessorialMilestoneStatus.PROPOSED) }
    }
}
