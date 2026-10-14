package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryAuditStore()
        val item = newRedeliveryAudit("code-309", "label-740")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
