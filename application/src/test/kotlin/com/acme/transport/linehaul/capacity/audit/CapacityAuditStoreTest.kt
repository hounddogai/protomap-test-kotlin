package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityAuditStore()
        val item = newCapacityAudit("code-894", "title-903")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
