package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRelayBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRelayBundleStore()
        val item = newGlobalRelayBundle("code-388", "label-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
