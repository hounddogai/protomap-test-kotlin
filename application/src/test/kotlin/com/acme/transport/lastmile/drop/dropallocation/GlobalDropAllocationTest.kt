package com.acme.transport.lastmile.drop.dropallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDropAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalDropAllocation("code-315", "title-969")
        assertEquals(GlobalDropAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalDropAllocation("code-315", "title-969")
        try {
            item.moveTo(GlobalDropAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDropAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalDropAllocation("code-315", "title-969")
        item.status = GlobalDropAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalDropAllocationStatus.REQUESTED) }
    }
}
