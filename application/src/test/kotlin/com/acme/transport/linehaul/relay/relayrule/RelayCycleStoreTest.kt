package com.acme.transport.linehaul.relay.relayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayCycleStore()
        val item = newRelayCycle("code-597", "label-623")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
