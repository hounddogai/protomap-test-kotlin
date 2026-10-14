package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnTierStore()
        val item = newLeaseReturnTier("code-531", "label-133")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
