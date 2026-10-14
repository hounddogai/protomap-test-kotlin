package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayClaimStore()
        val item = newRelayClaim("code-383", "title-211")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
