package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerDigestStore()
        val item = newBrokerDigest("code-307", "label-438")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
