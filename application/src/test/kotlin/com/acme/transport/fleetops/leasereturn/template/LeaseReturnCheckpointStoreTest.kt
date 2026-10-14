package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnCheckpointStore()
        val item = newLeaseReturnCheckpoint("code-869", "title-605")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
