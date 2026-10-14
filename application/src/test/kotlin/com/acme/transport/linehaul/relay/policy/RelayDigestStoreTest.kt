package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayDigestStore()
        val item = newRelayDigest("code-562", "name-61")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
