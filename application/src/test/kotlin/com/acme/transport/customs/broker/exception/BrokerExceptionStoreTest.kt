package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerExceptionStore()
        val item = newBrokerException("code-366", "name-278")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
