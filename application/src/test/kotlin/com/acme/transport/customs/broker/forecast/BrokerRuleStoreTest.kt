package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerRuleStore()
        val item = newBrokerRule("code-383", "name-71")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
