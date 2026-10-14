package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRelayOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRelayOverrideStore()
        val item = newTieredRelayOverride("code-990", "name-694")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
