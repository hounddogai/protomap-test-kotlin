package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnScoreStore()
        val item = newLeaseReturnScore("code-562", "name-813")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
