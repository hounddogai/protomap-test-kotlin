package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newClearanceMilestone("code-859", "label-531")
        assertEquals(ClearanceMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newClearanceMilestone("code-859", "label-531")
        try {
            item.moveTo(ClearanceMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newClearanceMilestone("code-859", "label-531")
        item.status = ClearanceMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(ClearanceMilestoneStatus.REQUESTED) }
    }
}
