package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTenderAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictTenderAllocation("code-438", "name-287")
        assertEquals(StrictTenderAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictTenderAllocation("code-438", "name-287")
        try {
            item.moveTo(StrictTenderAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTenderAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictTenderAllocation("code-438", "name-287")
        item.status = StrictTenderAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(StrictTenderAllocationStatus.DRAFT) }
    }
}
