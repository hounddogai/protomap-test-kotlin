package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerNoticeStore()
        val item = newBrokerNotice("code-267", "label-68")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
