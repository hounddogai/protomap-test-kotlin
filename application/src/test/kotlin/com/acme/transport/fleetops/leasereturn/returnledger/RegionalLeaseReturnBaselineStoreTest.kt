package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalLeaseReturnBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalLeaseReturnBaselineStore()
        val item = newRegionalLeaseReturnBaseline("code-423", "name-902")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
