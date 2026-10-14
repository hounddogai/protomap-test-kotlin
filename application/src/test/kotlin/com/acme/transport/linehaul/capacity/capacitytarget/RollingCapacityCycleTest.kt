package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCapacityCycleTest {
    @Test
    fun `starts in new`() {
        val item = newRollingCapacityCycle("code-331", "label-752")
        assertEquals(RollingCapacityCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingCapacityCycle("code-331", "label-752")
        try {
            item.moveTo(RollingCapacityCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCapacityCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingCapacityCycle("code-331", "label-752")
        item.status = RollingCapacityCycleStatus.FAILED
        assertFails("move") { item.moveTo(RollingCapacityCycleStatus.NEW) }
    }
}
