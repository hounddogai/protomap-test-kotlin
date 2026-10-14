package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerProfileStore()
        val item = newBrokerProfile("code-182", "title-660")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
