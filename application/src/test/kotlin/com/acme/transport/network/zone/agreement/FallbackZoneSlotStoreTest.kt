package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackZoneSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackZoneSlotStore()
        val item = newFallbackZoneSlot("code-918", "title-281")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
