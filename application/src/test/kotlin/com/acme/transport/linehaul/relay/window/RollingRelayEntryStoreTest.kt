package com.acme.transport.linehaul.relay.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRelayEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRelayEntryStore()
        val item = newRollingRelayEntry("code-60", "name-593")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
