package com.acme.transport.freightpay.audit.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditSnapshotStore()
        val item = newAuditSnapshot("code-656", "name-144")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
