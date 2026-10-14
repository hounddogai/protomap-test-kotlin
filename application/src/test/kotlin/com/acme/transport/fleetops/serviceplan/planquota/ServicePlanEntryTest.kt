package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanEntryTest {
    @Test
    fun `starts in open`() {
        val item = newServicePlanEntry("code-805", "title-193")
        assertEquals(ServicePlanEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newServicePlanEntry("code-805", "title-193")
        try {
            item.moveTo(ServicePlanEntryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanEntryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServicePlanEntry("code-805", "title-193")
        item.status = ServicePlanEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServicePlanEntryStatus.OPEN) }
    }
}
