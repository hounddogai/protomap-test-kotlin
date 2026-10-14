package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayThresholdStore()
        val item = newRelayThreshold("code-824", "title-287")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
