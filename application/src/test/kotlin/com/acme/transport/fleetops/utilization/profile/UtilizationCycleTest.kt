package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newUtilizationCycle("code-527", "label-687")
        assertEquals(UtilizationCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newUtilizationCycle("code-527", "label-687")
        try {
            item.moveTo(UtilizationCycleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationCycleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newUtilizationCycle("code-527", "label-687")
        item.status = UtilizationCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(UtilizationCycleStatus.REQUESTED) }
    }
}
