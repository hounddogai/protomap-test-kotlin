package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDockDoorScoreTest {
    @Test
    fun `starts in created`() {
        val item = newStrictDockDoorScore("code-724", "label-92")
        assertEquals(StrictDockDoorScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictDockDoorScore("code-724", "label-92")
        try {
            item.moveTo(StrictDockDoorScoreStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDockDoorScoreStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictDockDoorScore("code-724", "label-92")
        item.status = StrictDockDoorScoreStatus.RETIRED
        assertFails("move") { item.moveTo(StrictDockDoorScoreStatus.CREATED) }
    }
}
