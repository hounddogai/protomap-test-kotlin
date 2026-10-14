package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayRequestStore()
        val item = newRelayRequest("code-128", "title-70")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
