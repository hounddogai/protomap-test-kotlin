package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelBurnAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalFuelBurnAssignment("code-822", "name-881")
        assertEquals(GlobalFuelBurnAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalFuelBurnAssignment("code-822", "name-881")
        try {
            item.moveTo(GlobalFuelBurnAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelBurnAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalFuelBurnAssignment("code-822", "name-881")
        item.status = GlobalFuelBurnAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalFuelBurnAssignmentStatus.REQUESTED) }
    }
}
