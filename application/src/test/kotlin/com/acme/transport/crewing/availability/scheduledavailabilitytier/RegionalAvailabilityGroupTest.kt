package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAvailabilityGroupTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalAvailabilityGroup("code-26", "title-880")
        assertEquals(RegionalAvailabilityGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalAvailabilityGroup("code-26", "title-880")
        try {
            item.moveTo(RegionalAvailabilityGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAvailabilityGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalAvailabilityGroup("code-26", "title-880")
        item.status = RegionalAvailabilityGroupStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalAvailabilityGroupStatus.CREATED) }
    }
}
