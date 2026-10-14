package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayRollupStore()
        val item = newRelayRollup("code-877", "title-217")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
