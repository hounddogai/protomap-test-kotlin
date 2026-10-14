package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnRollupStore()
        val item = newLeaseReturnRollup("code-591", "name-222")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
