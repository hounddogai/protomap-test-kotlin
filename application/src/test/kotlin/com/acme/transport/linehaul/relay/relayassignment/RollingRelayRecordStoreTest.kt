package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRelayRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRelayRecordStore()
        val item = newRollingRelayRecord("code-848", "title-43")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
