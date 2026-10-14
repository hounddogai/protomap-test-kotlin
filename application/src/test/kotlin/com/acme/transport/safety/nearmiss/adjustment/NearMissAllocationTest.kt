package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newNearMissAllocation("code-428", "name-155")
        assertEquals(NearMissAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newNearMissAllocation("code-428", "name-155")
        try {
            item.moveTo(NearMissAllocationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissAllocationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNearMissAllocation("code-428", "name-155")
        item.status = NearMissAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(NearMissAllocationStatus.DRAFT) }
    }
}
