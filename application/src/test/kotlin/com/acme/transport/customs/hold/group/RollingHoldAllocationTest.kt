package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingHoldAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingHoldAllocation("code-420", "title-896")
        assertEquals(RollingHoldAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingHoldAllocation("code-420", "title-896")
        try {
            item.moveTo(RollingHoldAllocationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingHoldAllocationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingHoldAllocation("code-420", "title-896")
        item.status = RollingHoldAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingHoldAllocationStatus.PROPOSED) }
    }
}
