package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRelayAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveRelayAssignment("code-442", "title-579")
        assertEquals(AdaptiveRelayAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveRelayAssignment("code-442", "title-579")
        try {
            item.moveTo(AdaptiveRelayAssignmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRelayAssignmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveRelayAssignment("code-442", "title-579")
        item.status = AdaptiveRelayAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveRelayAssignmentStatus.PLANNED) }
    }
}
