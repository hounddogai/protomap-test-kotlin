package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireAuditStore()
        val item = newTireAudit("code-744", "title-908")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
