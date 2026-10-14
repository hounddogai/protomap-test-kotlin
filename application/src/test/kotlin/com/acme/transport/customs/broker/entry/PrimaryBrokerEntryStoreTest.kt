package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryBrokerEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryBrokerEntryStore()
        val item = newPrimaryBrokerEntry("code-826", "name-161")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
