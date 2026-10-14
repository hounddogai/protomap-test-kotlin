package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictLeaseReturnBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictLeaseReturnBaselineStore()
        val item = newStrictLeaseReturnBaseline("code-901", "label-59")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
