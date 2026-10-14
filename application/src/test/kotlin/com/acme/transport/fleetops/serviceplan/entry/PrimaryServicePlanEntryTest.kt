package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServicePlanEntryTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryServicePlanEntry("code-897", "name-493")
        assertEquals(PrimaryServicePlanEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryServicePlanEntry("code-897", "name-493")
        try {
            item.moveTo(PrimaryServicePlanEntryStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServicePlanEntryStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryServicePlanEntry("code-897", "name-493")
        item.status = PrimaryServicePlanEntryStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryServicePlanEntryStatus.NEW) }
    }
}
