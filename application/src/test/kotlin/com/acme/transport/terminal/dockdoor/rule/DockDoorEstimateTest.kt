package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorEstimate("code-929", "title-341")
        assertEquals(DockDoorEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDockDoorEstimate("code-929", "title-341")
        try {
            item.moveTo(DockDoorEstimateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorEstimateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorEstimate("code-929", "title-341")
        item.status = DockDoorEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorEstimateStatus.REQUESTED) }
    }
}
