package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRedeliveryAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalRedeliveryAssignment("code-660", "name-971")
        assertEquals(RegionalRedeliveryAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalRedeliveryAssignment("code-660", "name-971")
        try {
            item.moveTo(RegionalRedeliveryAssignmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRedeliveryAssignmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalRedeliveryAssignment("code-660", "name-971")
        item.status = RegionalRedeliveryAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalRedeliveryAssignmentStatus.DRAFT) }
    }
}
