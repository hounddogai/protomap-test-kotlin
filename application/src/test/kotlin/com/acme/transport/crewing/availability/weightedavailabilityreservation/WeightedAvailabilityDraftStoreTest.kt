package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAvailabilityDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAvailabilityDraftStore()
        val item = newWeightedAvailabilityDraft("code-828", "name-416")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
