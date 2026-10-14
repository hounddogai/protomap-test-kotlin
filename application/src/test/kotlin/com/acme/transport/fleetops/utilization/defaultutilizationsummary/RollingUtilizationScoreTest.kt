package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingUtilizationScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingUtilizationScore("code-692", "name-655")
        assertEquals(RollingUtilizationScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingUtilizationScore("code-692", "name-655")
        try {
            item.moveTo(RollingUtilizationScoreStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingUtilizationScoreStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingUtilizationScore("code-692", "name-655")
        item.status = RollingUtilizationScoreStatus.CLOSED
        assertFails("move") { item.moveTo(RollingUtilizationScoreStatus.DRAFT) }
    }
}
