package com.acme.transport.fleetops.leasereturn.returnbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyLeaseReturnBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyLeaseReturnBundleStore()
        val item = newLegacyLeaseReturnBundle("code-53", "name-317")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
