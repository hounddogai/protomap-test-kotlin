package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceAuditStore()
        val item = newClearanceAudit("code-370", "title-252")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
