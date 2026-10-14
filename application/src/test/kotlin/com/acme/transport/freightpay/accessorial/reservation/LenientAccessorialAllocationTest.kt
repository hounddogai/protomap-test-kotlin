package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAccessorialAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientAccessorialAllocation("code-572", "name-282")
        assertEquals(LenientAccessorialAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientAccessorialAllocation("code-572", "name-282")
        try {
            item.moveTo(LenientAccessorialAllocationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAccessorialAllocationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientAccessorialAllocation("code-572", "name-282")
        item.status = LenientAccessorialAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientAccessorialAllocationStatus.PLANNED) }
    }
}
