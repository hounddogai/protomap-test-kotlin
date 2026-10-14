package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDockDoorTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingDockDoorTier("code-216", "name-276")
        assertEquals(RollingDockDoorTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRollingDockDoorTier("code-216", "name-276")
        try {
            item.moveTo(RollingDockDoorTierStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDockDoorTierStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingDockDoorTier("code-216", "name-276")
        item.status = RollingDockDoorTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingDockDoorTierStatus.PROPOSED) }
    }
}
