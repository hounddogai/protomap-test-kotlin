package com.acme.transport.crewing.availability.availabilityledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAvailabilityPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedAvailabilityPreference("code-71", "title-266")
        assertEquals(CappedAvailabilityPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedAvailabilityPreference("code-71", "title-266")
        try {
            item.moveTo(CappedAvailabilityPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAvailabilityPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedAvailabilityPreference("code-71", "title-266")
        item.status = CappedAvailabilityPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedAvailabilityPreferenceStatus.REQUESTED) }
    }
}
