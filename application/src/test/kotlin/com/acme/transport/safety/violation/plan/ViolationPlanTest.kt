package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newViolationPlan("code-635", "label-668")
        assertEquals(ViolationPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newViolationPlan("code-635", "label-668")
        try {
            item.moveTo(ViolationPlanStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationPlanStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newViolationPlan("code-635", "label-668")
        item.status = ViolationPlanStatus.CLOSED
        assertFails("move") { item.moveTo(ViolationPlanStatus.DRAFT) }
    }
}
