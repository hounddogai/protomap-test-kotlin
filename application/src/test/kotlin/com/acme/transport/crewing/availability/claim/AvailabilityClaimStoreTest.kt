package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityClaimStore()
        val item = newAvailabilityClaim("code-236", "label-109")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
