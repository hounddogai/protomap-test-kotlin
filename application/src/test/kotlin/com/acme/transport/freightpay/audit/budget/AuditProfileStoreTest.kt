package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditProfileStore()
        val item = newAuditProfile("code-539", "name-962")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
