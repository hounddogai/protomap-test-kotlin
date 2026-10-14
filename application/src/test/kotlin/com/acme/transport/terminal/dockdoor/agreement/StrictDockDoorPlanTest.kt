package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDockDoorPlanTest {
    @Test
    fun `starts in created`() {
        val item = newStrictDockDoorPlan("code-349", "label-255")
        assertEquals(StrictDockDoorPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newStrictDockDoorPlan("code-349", "label-255")
        try {
            item.moveTo(StrictDockDoorPlanStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDockDoorPlanStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictDockDoorPlan("code-349", "label-255")
        item.status = StrictDockDoorPlanStatus.RETIRED
        assertFails("move") { item.moveTo(StrictDockDoorPlanStatus.CREATED) }
    }
}
