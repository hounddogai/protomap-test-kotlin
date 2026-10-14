package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRelayWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRelayWindowStore()
        val item = newStrictRelayWindow("code-241", "label-556")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
