package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newParcelAllocation("code-312", "name-529")
        assertEquals(ParcelAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newParcelAllocation("code-312", "name-529")
        try {
            item.moveTo(ParcelAllocationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelAllocationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newParcelAllocation("code-312", "name-529")
        item.status = ParcelAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(ParcelAllocationStatus.DRAFT) }
    }
}
