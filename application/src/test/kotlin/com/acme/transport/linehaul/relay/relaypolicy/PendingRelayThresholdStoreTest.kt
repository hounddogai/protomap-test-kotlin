package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRelayThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRelayThresholdStore()
        val item = newPendingRelayThreshold("code-787", "name-321")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
