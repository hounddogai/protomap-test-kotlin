package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnManifestStore()
        val item = newLeaseReturnManifest("code-650", "label-550")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
