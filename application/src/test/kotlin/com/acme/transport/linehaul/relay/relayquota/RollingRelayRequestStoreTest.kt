package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRelayRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRelayRequestStore()
        val item = newRollingRelayRequest("code-10", "label-291")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
