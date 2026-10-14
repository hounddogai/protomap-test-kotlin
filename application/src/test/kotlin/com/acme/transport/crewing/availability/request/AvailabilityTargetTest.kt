package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityTargetTest {
    @Test
    fun `starts in open`() {
        val item = newAvailabilityTarget("code-930", "name-802")
        assertEquals(AvailabilityTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAvailabilityTarget("code-930", "name-802")
        try {
            item.moveTo(AvailabilityTargetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityTargetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAvailabilityTarget("code-930", "name-802")
        item.status = AvailabilityTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(AvailabilityTargetStatus.OPEN) }
    }
}
