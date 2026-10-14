package com.acme.transport.crewing.availability.availabilityaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newAvailabilityOverride("code-851", "label-673")
        assertEquals(AvailabilityOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAvailabilityOverride("code-851", "label-673")
        try {
            item.moveTo(AvailabilityOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAvailabilityOverride("code-851", "label-673")
        item.status = AvailabilityOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(AvailabilityOverrideStatus.REQUESTED) }
    }
}
