package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAvailabilityTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAvailabilityTierStore()
        val item = newAdaptiveAvailabilityTier("code-712", "title-988")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
