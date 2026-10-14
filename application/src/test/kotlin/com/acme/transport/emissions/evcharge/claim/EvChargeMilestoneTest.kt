package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newEvChargeMilestone("code-22", "name-737")
        assertEquals(EvChargeMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newEvChargeMilestone("code-22", "name-737")
        try {
            item.moveTo(EvChargeMilestoneStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeMilestoneStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEvChargeMilestone("code-22", "name-737")
        item.status = EvChargeMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(EvChargeMilestoneStatus.IDLE) }
    }
}
