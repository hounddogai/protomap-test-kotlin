package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackBackhaulPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackBackhaulPreferenceStore()
        val item = newFallbackBackhaulPreference("code-889", "name-221")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
