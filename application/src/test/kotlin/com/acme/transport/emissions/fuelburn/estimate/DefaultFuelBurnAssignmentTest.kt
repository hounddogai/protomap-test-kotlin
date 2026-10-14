package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultFuelBurnAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultFuelBurnAssignment("code-222", "title-900")
        assertEquals(DefaultFuelBurnAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefaultFuelBurnAssignment("code-222", "title-900")
        try {
            item.moveTo(DefaultFuelBurnAssignmentStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultFuelBurnAssignmentStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultFuelBurnAssignment("code-222", "title-900")
        item.status = DefaultFuelBurnAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultFuelBurnAssignmentStatus.DRAFT) }
    }
}
