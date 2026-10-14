package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRelayDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRelayDigestStore()
        val item = newAdaptiveRelayDigest("code-649", "label-627")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
