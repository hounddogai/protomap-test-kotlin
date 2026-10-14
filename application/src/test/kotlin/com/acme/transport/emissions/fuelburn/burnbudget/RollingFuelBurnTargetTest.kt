package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingFuelBurnTarget("code-518", "title-608")
        assertEquals(RollingFuelBurnTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRollingFuelBurnTarget("code-518", "title-608")
        try {
            item.moveTo(RollingFuelBurnTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingFuelBurnTarget("code-518", "title-608")
        item.status = RollingFuelBurnTargetStatus.STOPPED
        assertFails("move") { item.moveTo(RollingFuelBurnTargetStatus.IDLE) }
    }
}
