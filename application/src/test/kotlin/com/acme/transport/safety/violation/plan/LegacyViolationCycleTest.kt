package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyViolationCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyViolationCycle("code-17", "label-687")
        assertEquals(LegacyViolationCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyViolationCycle("code-17", "label-687")
        try {
            item.moveTo(LegacyViolationCycleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyViolationCycleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyViolationCycle("code-17", "label-687")
        item.status = LegacyViolationCycleStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyViolationCycleStatus.IDLE) }
    }
}
