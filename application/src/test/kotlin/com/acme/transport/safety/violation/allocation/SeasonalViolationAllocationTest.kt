package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalViolationAllocationTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalViolationAllocation("code-2", "label-645")
        assertEquals(SeasonalViolationAllocationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalViolationAllocation("code-2", "label-645")
        try {
            item.moveTo(SeasonalViolationAllocationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalViolationAllocationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalViolationAllocation("code-2", "label-645")
        item.status = SeasonalViolationAllocationStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalViolationAllocationStatus.OPEN) }
    }
}
