package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBrokerWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBrokerWindowStore()
        val item = newLegacyBrokerWindow("code-814", "name-636")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
