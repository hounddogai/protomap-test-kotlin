package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayOverrideStore()
        val item = newRelayOverride("code-527", "name-5")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
