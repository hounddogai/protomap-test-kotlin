package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayPreferenceStore()
        val item = newRelayPreference("code-118", "label-61")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
