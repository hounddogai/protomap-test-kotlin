package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictLeaseReturnMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictLeaseReturnMilestoneStore()
        val item = newStrictLeaseReturnMilestone("code-337", "label-455")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
