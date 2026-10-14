package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAvailabilityAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientAvailabilityAllocation("code-200", "title-193")
        assertEquals(LenientAvailabilityAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLenientAvailabilityAllocation("code-200", "title-193")
        try {
            item.moveTo(LenientAvailabilityAllocationStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAvailabilityAllocationStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientAvailabilityAllocation("code-200", "title-193")
        item.status = LenientAvailabilityAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(LenientAvailabilityAllocationStatus.DRAFT) }
    }
}
