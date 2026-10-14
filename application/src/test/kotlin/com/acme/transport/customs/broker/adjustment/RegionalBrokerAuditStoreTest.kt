package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalBrokerAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalBrokerAuditStore()
        val item = newRegionalBrokerAudit("code-213", "title-148")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
