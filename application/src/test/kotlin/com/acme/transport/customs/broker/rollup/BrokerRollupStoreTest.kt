package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerRollupStore()
        val item = newBrokerRollup("code-709", "name-79")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
