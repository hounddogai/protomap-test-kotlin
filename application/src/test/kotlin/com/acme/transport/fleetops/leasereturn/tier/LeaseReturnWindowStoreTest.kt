package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnWindowStore()
        val item = newLeaseReturnWindow("code-980", "label-103")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
