package com.acme.transport.terminal.dwell.dwellgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDwellPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingDwellPlan("code-620", "name-454")
        assertEquals(RollingDwellPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingDwellPlan("code-620", "name-454")
        try {
            item.moveTo(RollingDwellPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDwellPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingDwellPlan("code-620", "name-454")
        item.status = RollingDwellPlanStatus.STOPPED
        assertFails("move") { item.moveTo(RollingDwellPlanStatus.IDLE) }
    }
}
