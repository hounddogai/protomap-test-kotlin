package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityPlan("code-488", "label-2")
        assertEquals(CapacityPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCapacityPlan("code-488", "label-2")
        try {
            item.moveTo(CapacityPlanStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityPlanStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityPlan("code-488", "label-2")
        item.status = CapacityPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityPlanStatus.REQUESTED) }
    }
}
