package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerQuotaStore()
        val item = newBrokerQuota("code-943", "name-29")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
