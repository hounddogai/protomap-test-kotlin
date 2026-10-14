package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayWindowStore()
        val item = newRelayWindow("code-660", "title-95")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
