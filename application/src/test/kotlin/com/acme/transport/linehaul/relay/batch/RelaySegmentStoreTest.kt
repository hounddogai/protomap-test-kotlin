package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelaySegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelaySegmentStore()
        val item = newRelaySegment("code-239", "label-487")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
