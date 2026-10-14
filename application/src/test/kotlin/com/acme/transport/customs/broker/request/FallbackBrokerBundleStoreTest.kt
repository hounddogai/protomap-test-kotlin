package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackBrokerBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackBrokerBundleStore()
        val item = newFallbackBrokerBundle("code-180", "name-432")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
