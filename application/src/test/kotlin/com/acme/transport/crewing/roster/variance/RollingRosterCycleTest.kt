package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRosterCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingRosterCycle("code-244", "name-480")
        assertEquals(RollingRosterCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingRosterCycle("code-244", "name-480")
        try {
            item.moveTo(RollingRosterCycleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRosterCycleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingRosterCycle("code-244", "name-480")
        item.status = RollingRosterCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingRosterCycleStatus.PLANNED) }
    }
}
