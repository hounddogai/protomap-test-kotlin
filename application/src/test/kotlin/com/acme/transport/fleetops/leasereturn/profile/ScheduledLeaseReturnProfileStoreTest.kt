package com.acme.transport.fleetops.leasereturn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLeaseReturnProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLeaseReturnProfileStore()
        val item = newScheduledLeaseReturnProfile("code-395", "title-967")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
