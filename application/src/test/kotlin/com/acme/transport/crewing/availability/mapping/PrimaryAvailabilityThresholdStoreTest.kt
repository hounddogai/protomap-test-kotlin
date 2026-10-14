package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAvailabilityThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAvailabilityThresholdStore()
        val item = newPrimaryAvailabilityThreshold("code-930", "label-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
