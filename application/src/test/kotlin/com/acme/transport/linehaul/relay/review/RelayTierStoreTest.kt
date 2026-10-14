package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayTierStore()
        val item = newRelayTier("code-530", "name-487")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
