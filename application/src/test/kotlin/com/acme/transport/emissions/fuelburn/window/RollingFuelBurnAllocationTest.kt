package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingFuelBurnAllocation("code-508", "name-420")
        assertEquals(RollingFuelBurnAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingFuelBurnAllocation("code-508", "name-420")
        try {
            item.moveTo(RollingFuelBurnAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingFuelBurnAllocation("code-508", "name-420")
        item.status = RollingFuelBurnAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(RollingFuelBurnAllocationStatus.DRAFT) }
    }
}
