package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingBrokerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingBrokerScoreStore()
        val item = newPendingBrokerScore("code-546", "name-706")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
