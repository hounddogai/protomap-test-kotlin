package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientLeaseReturnManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientLeaseReturnManifestStore()
        val item = newLenientLeaseReturnManifest("code-553", "title-540")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
