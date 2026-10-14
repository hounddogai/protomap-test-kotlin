package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayPolicyStore()
        val item = newRelayPolicy("code-991", "label-473")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
