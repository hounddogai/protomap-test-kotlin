package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalBrokerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalBrokerOverrideStore()
        val item = newGlobalBrokerOverride("code-376", "title-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
