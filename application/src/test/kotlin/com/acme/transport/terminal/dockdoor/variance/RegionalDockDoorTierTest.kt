package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDockDoorTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalDockDoorTier("code-5", "label-813")
        assertEquals(RegionalDockDoorTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRegionalDockDoorTier("code-5", "label-813")
        try {
            item.moveTo(RegionalDockDoorTierStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDockDoorTierStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalDockDoorTier("code-5", "label-813")
        item.status = RegionalDockDoorTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalDockDoorTierStatus.PROPOSED) }
    }
}
