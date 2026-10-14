package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDockDoorTierTest {
    @Test
    fun `starts in created`() {
        val item = newPendingDockDoorTier("code-553", "label-903")
        assertEquals(PendingDockDoorTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingDockDoorTier("code-553", "label-903")
        try {
            item.moveTo(PendingDockDoorTierStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDockDoorTierStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingDockDoorTier("code-553", "label-903")
        item.status = PendingDockDoorTierStatus.RETIRED
        assertFails("move") { item.moveTo(PendingDockDoorTierStatus.CREATED) }
    }
}
