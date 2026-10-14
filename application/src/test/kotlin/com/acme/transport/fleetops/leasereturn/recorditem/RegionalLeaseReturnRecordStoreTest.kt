package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalLeaseReturnRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalLeaseReturnRecordStore()
        val item = newRegionalLeaseReturnRecord("code-649", "name-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
