package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCapacityPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCapacityPreferenceStore()
        val item = newFallbackCapacityPreference("code-466", "title-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
