package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newDwellAllocation("code-456", "label-810")
        assertEquals(DwellAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDwellAllocation("code-456", "label-810")
        try {
            item.moveTo(DwellAllocationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellAllocationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDwellAllocation("code-456", "label-810")
        item.status = DwellAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(DwellAllocationStatus.PLANNED) }
    }
}
