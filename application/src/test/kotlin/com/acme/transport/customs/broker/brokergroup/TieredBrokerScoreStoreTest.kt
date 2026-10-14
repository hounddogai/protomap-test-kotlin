package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredBrokerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredBrokerScoreStore()
        val item = newTieredBrokerScore("code-406", "title-686")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
