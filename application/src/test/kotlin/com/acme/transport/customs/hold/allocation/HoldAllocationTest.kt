package com.acme.transport.customs.hold.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newHoldAllocation("code-45", "title-598")
        assertEquals(HoldAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newHoldAllocation("code-45", "title-598")
        try {
            item.moveTo(HoldAllocationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldAllocationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHoldAllocation("code-45", "title-598")
        item.status = HoldAllocationStatus.FAILED
        assertFails("move") { item.moveTo(HoldAllocationStatus.NEW) }
    }
}
