package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAvailabilityBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAvailabilityBaselineStore()
        val item = newPrimaryAvailabilityBaseline("code-416", "name-199")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
