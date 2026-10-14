package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultLeaseReturnEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultLeaseReturnEntryStore()
        val item = newDefaultLeaseReturnEntry("code-349", "name-312")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
