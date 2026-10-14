package com.acme.transport.linehaul.relay.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRelayTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRelayTemplateStore()
        val item = newDefaultRelayTemplate("code-421", "label-662")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
