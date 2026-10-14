package com.acme.transport.crewing.availability.availabilityaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityOverrideStore()
        val item = newAvailabilityOverride("code-851", "label-673")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
