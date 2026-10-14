package com.acme.transport.fleetops.leasereturn.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnOverrideStore()
        val item = newLeaseReturnOverride("code-976", "title-112")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
