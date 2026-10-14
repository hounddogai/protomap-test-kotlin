package com.acme.transport.customs.broker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBrokerQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBrokerQuotaStore()
        val item = newRollingBrokerQuota("code-119", "label-910")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
