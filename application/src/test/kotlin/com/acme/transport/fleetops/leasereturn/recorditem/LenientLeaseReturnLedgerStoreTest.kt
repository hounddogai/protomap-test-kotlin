package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientLeaseReturnLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientLeaseReturnLedgerStore()
        val item = newLenientLeaseReturnLedger("code-910", "label-373")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
