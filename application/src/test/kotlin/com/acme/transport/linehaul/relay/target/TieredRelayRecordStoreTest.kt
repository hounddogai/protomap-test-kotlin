package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRelayRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRelayRecordStore()
        val item = newTieredRelayRecord("code-813", "name-338")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
