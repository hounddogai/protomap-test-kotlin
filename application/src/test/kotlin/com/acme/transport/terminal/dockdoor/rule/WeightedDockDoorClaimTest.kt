package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDockDoorClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedDockDoorClaim("code-255", "label-569")
        assertEquals(WeightedDockDoorClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newWeightedDockDoorClaim("code-255", "label-569")
        try {
            item.moveTo(WeightedDockDoorClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDockDoorClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedDockDoorClaim("code-255", "label-569")
        item.status = WeightedDockDoorClaimStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedDockDoorClaimStatus.IDLE) }
    }
}
