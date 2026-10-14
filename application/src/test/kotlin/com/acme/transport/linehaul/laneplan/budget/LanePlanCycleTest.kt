package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanCycleTest {
    @Test
    fun `starts in open`() {
        val item = newLanePlanCycle("code-354", "label-744")
        assertEquals(LanePlanCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLanePlanCycle("code-354", "label-744")
        try {
            item.moveTo(LanePlanCycleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanCycleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLanePlanCycle("code-354", "label-744")
        item.status = LanePlanCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(LanePlanCycleStatus.OPEN) }
    }
}
