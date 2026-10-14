package com.acme.transport.fleetops.leasereturn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLeaseReturnGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLeaseReturnGroupStore()
        val item = newRollingLeaseReturnGroup("code-937", "name-768")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
