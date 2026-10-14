package com.acme.transport.crewing.availability.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityTierTest {
    @Test
    fun `starts in open`() {
        val item = newAvailabilityTier("code-121", "label-386")
        assertEquals(AvailabilityTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAvailabilityTier("code-121", "label-386")
        try {
            item.moveTo(AvailabilityTierStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityTierStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAvailabilityTier("code-121", "label-386")
        item.status = AvailabilityTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(AvailabilityTierStatus.OPEN) }
    }
}
