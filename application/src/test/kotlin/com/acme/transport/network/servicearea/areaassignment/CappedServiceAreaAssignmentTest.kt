package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServiceAreaAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedServiceAreaAssignment("code-118", "name-513")
        assertEquals(CappedServiceAreaAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCappedServiceAreaAssignment("code-118", "name-513")
        try {
            item.moveTo(CappedServiceAreaAssignmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServiceAreaAssignmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedServiceAreaAssignment("code-118", "name-513")
        item.status = CappedServiceAreaAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(CappedServiceAreaAssignmentStatus.DRAFT) }
    }
}
