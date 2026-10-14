package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalCapacityProfile("code-630", "name-469")
        assertEquals(RegionalCapacityProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalCapacityProfile("code-630", "name-469")
        try {
            item.moveTo(RegionalCapacityProfileStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityProfileStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalCapacityProfile("code-630", "name-469")
        item.status = RegionalCapacityProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalCapacityProfileStatus.REQUESTED) }
    }
}
