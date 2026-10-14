package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayEstimateStore()
        val item = newRelayEstimate("code-248", "name-558")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
