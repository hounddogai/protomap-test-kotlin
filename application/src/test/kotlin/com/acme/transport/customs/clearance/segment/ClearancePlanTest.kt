package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearancePlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearancePlan("code-864", "label-349")
        assertEquals(ClearancePlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newClearancePlan("code-864", "label-349")
        try {
            item.moveTo(ClearancePlanStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearancePlanStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearancePlan("code-864", "label-349")
        item.status = ClearancePlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearancePlanStatus.PROPOSED) }
    }
}
