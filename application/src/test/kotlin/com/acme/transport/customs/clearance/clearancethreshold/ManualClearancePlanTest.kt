package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualClearancePlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualClearancePlan("code-360", "name-532")
        assertEquals(ManualClearancePlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualClearancePlan("code-360", "name-532")
        try {
            item.moveTo(ManualClearancePlanStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualClearancePlanStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualClearancePlan("code-360", "name-532")
        item.status = ManualClearancePlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualClearancePlanStatus.PROPOSED) }
    }
}
