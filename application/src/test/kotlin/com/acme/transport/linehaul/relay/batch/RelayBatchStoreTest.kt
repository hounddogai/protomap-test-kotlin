package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayBatchStore()
        val item = newRelayBatch("code-676", "label-620")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
