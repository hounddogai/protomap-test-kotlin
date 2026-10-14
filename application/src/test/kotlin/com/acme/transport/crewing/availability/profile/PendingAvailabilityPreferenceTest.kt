package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAvailabilityPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingAvailabilityPreference("code-718", "label-872")
        assertEquals(PendingAvailabilityPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPendingAvailabilityPreference("code-718", "label-872")
        try {
            item.moveTo(PendingAvailabilityPreferenceStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAvailabilityPreferenceStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingAvailabilityPreference("code-718", "label-872")
        item.status = PendingAvailabilityPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingAvailabilityPreferenceStatus.REQUESTED) }
    }
}
