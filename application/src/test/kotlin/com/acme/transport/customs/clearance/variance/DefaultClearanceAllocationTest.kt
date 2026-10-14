package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultClearanceAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultClearanceAllocation("code-312", "label-948")
        assertEquals(DefaultClearanceAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultClearanceAllocation("code-312", "label-948")
        try {
            item.moveTo(DefaultClearanceAllocationStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultClearanceAllocationStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultClearanceAllocation("code-312", "label-948")
        item.status = DefaultClearanceAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultClearanceAllocationStatus.REQUESTED) }
    }
}
