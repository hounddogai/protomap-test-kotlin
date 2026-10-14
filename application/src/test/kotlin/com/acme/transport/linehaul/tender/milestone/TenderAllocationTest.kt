package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newTenderAllocation("code-792", "label-289")
        assertEquals(TenderAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTenderAllocation("code-792", "label-289")
        try {
            item.moveTo(TenderAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTenderAllocation("code-792", "label-289")
        item.status = TenderAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(TenderAllocationStatus.CREATED) }
    }
}
