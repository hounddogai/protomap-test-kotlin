package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDwellAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledDwellAllocation("code-129", "title-321")
        assertEquals(ScheduledDwellAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledDwellAllocation("code-129", "title-321")
        try {
            item.moveTo(ScheduledDwellAllocationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDwellAllocationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledDwellAllocation("code-129", "title-321")
        item.status = ScheduledDwellAllocationStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledDwellAllocationStatus.NEW) }
    }
}
