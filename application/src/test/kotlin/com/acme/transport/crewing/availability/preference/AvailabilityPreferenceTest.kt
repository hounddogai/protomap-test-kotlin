package com.acme.transport.crewing.availability.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newAvailabilityPreference("code-872", "title-146")
        assertEquals(AvailabilityPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAvailabilityPreference("code-872", "title-146")
        try {
            item.moveTo(AvailabilityPreferenceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityPreferenceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAvailabilityPreference("code-872", "title-146")
        item.status = AvailabilityPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(AvailabilityPreferenceStatus.PLANNED) }
    }
}
