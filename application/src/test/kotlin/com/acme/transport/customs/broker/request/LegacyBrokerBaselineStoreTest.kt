package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBrokerBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBrokerBaselineStore()
        val item = newLegacyBrokerBaseline("code-967", "name-29")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
