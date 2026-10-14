package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRelayVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRelayVarianceStore()
        val item = newGlobalRelayVariance("code-653", "label-136")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
