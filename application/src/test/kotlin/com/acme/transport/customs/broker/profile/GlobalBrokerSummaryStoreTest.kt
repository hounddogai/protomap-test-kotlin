package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalBrokerSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalBrokerSummaryStore()
        val item = newGlobalBrokerSummary("code-188", "name-298")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
