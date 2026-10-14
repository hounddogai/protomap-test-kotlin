package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerPolicyStore()
        val item = newBrokerPolicy("code-258", "title-96")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
