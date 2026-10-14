package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneAuditStore()
        val item = newZoneAudit("code-611", "name-99")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
