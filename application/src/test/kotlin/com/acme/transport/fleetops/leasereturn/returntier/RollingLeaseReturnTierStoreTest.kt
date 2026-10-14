package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLeaseReturnTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLeaseReturnTierStore()
        val item = newRollingLeaseReturnTier("code-542", "name-980")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
