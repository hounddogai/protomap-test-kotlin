package com.acme.transport.network.cutoff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newCutoffAllocation("code-770", "name-239")
        assertEquals(CutoffAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCutoffAllocation("code-770", "name-239")
        try {
            item.moveTo(CutoffAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCutoffAllocation("code-770", "name-239")
        item.status = CutoffAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(CutoffAllocationStatus.DRAFT) }
    }
}
