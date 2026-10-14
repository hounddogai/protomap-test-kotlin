package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServiceAreaMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newStrictServiceAreaMilestone("code-353", "label-764")
        assertEquals(StrictServiceAreaMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictServiceAreaMilestone("code-353", "label-764")
        try {
            item.moveTo(StrictServiceAreaMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServiceAreaMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictServiceAreaMilestone("code-353", "label-764")
        item.status = StrictServiceAreaMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(StrictServiceAreaMilestoneStatus.CREATED) }
    }
}
