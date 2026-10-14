package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTransitTimePreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTransitTimePreferenceStore()
        val item = newAdaptiveTransitTimePreference("code-664", "label-890")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
