package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubAuditStore()
        val item = newHubAudit("code-137", "name-587")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
