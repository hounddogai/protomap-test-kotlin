package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnMappingStore()
        val item = newLeaseReturnMapping("code-705", "label-137")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
