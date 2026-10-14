package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRelayPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRelayPolicyStore()
        val item = newFallbackRelayPolicy("code-662", "title-672")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
