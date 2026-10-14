package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualBrokerQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualBrokerQuotaStore()
        val item = newManualBrokerQuota("code-864", "name-293")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
