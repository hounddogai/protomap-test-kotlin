package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newViolationAllocation("code-562", "label-131")
        assertEquals(ViolationAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newViolationAllocation("code-562", "label-131")
        try {
            item.moveTo(ViolationAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newViolationAllocation("code-562", "label-131")
        item.status = ViolationAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(ViolationAllocationStatus.CREATED) }
    }
}
