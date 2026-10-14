package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServicePlanEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedServicePlanEntry("code-465", "title-853")
        assertEquals(CappedServicePlanEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedServicePlanEntry("code-465", "title-853")
        try {
            item.moveTo(CappedServicePlanEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServicePlanEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedServicePlanEntry("code-465", "title-853")
        item.status = CappedServicePlanEntryStatus.CLOSED
        assertFails("move") { item.moveTo(CappedServicePlanEntryStatus.DRAFT) }
    }
}
