package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalNearMissMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalNearMissMilestone("code-950", "name-632")
        assertEquals(RegionalNearMissMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalNearMissMilestone("code-950", "name-632")
        try {
            item.moveTo(RegionalNearMissMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalNearMissMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalNearMissMilestone("code-950", "name-632")
        item.status = RegionalNearMissMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalNearMissMilestoneStatus.CREATED) }
    }
}
