package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayTargetStore()
        val item = newRelayTarget("code-134", "title-66")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
