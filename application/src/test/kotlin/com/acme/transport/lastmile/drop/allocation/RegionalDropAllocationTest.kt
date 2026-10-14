package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDropAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalDropAllocation("code-555", "name-374")
        assertEquals(RegionalDropAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalDropAllocation("code-555", "name-374")
        try {
            item.moveTo(RegionalDropAllocationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDropAllocationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalDropAllocation("code-555", "name-374")
        item.status = RegionalDropAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalDropAllocationStatus.PLANNED) }
    }
}
