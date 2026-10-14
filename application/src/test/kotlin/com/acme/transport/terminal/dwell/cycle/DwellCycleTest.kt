package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellCycleTest {
    @Test
    fun `starts in new`() {
        val item = newDwellCycle("code-24", "name-136")
        assertEquals(DwellCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDwellCycle("code-24", "name-136")
        try {
            item.moveTo(DwellCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellCycle("code-24", "name-136")
        item.status = DwellCycleStatus.FAILED
        assertFails("move") { item.moveTo(DwellCycleStatus.NEW) }
    }
}
