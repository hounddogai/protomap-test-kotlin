package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerDraftStore()
        val item = newBrokerDraft("code-109", "label-310")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
