package com.acme.transport.linehaul.relay.relaygroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRelayGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRelayGroupStore()
        val item = newLenientRelayGroup("code-606", "title-890")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
