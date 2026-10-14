package com.acme.transport.customs.broker.brokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictBrokerDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictBrokerDraftStore()
        val item = newStrictBrokerDraft("code-955", "label-769")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
