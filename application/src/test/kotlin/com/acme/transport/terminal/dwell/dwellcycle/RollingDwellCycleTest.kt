package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDwellCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingDwellCycle("code-707", "title-293")
        assertEquals(RollingDwellCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingDwellCycle("code-707", "title-293")
        try {
            item.moveTo(RollingDwellCycleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDwellCycleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingDwellCycle("code-707", "title-293")
        item.status = RollingDwellCycleStatus.STOPPED
        assertFails("move") { item.moveTo(RollingDwellCycleStatus.IDLE) }
    }
}
