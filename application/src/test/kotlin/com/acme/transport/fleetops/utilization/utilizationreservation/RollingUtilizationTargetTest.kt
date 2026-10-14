package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingUtilizationTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingUtilizationTarget("code-726", "name-416")
        assertEquals(RollingUtilizationTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingUtilizationTarget("code-726", "name-416")
        try {
            item.moveTo(RollingUtilizationTargetStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingUtilizationTargetStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingUtilizationTarget("code-726", "name-416")
        item.status = RollingUtilizationTargetStatus.CLOSED
        assertFails("move") { item.moveTo(RollingUtilizationTargetStatus.DRAFT) }
    }
}
