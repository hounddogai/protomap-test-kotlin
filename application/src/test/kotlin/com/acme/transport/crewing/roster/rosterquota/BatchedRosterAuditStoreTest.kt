package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRosterAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRosterAuditStore()
        val item = newBatchedRosterAudit("code-245", "label-639")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
