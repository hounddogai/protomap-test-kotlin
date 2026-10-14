package com.acme.transport.fleetops.utilization.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledUtilizationCycleTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledUtilizationCycle("code-620", "name-559")
        assertEquals(ScheduledUtilizationCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledUtilizationCycle("code-620", "name-559")
        try {
            item.moveTo(ScheduledUtilizationCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledUtilizationCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledUtilizationCycle("code-620", "name-559")
        item.status = ScheduledUtilizationCycleStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledUtilizationCycleStatus.NEW) }
    }
}
