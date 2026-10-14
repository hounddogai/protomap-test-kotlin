package com.acme.transport.fleetops.leasereturn.returnpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalLeaseReturnRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalLeaseReturnRollupStore()
        val item = newGlobalLeaseReturnRollup("code-526", "label-193")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
