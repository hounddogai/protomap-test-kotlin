package com.acme.transport.crewing.availability.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityPreferenceStore()
        val item = newAvailabilityPreference("code-872", "title-146")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
