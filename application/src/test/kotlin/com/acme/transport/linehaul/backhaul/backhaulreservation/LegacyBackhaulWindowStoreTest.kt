package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBackhaulWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBackhaulWindowStore()
        val item = newLegacyBackhaulWindow("code-956", "title-361")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
