package com.acme.transport.customs.clearance.clearanceestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictClearanceEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newStrictClearanceEstimate("code-956", "name-287")
        assertEquals(StrictClearanceEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictClearanceEstimate("code-956", "name-287")
        try {
            item.moveTo(StrictClearanceEstimateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictClearanceEstimateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictClearanceEstimate("code-956", "name-287")
        item.status = StrictClearanceEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictClearanceEstimateStatus.OPEN) }
    }
}
