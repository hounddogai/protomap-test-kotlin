package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackZoneThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackZoneThresholdStore()
        val item = newFallbackZoneThreshold("code-690", "name-713")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
