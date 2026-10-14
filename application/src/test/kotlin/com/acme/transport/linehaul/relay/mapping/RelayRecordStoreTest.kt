package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayRecordStore()
        val item = newRelayRecord("code-502", "title-168")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
