package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newAvailabilityGroup("code-48", "name-621")
        assertEquals(AvailabilityGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAvailabilityGroup("code-48", "name-621")
        try {
            item.moveTo(AvailabilityGroupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityGroupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAvailabilityGroup("code-48", "name-621")
        item.status = AvailabilityGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(AvailabilityGroupStatus.REQUESTED) }
    }
}
