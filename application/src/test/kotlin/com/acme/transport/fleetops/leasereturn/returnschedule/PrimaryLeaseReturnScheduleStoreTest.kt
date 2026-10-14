package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryLeaseReturnScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryLeaseReturnScheduleStore()
        val item = newPrimaryLeaseReturnSchedule("code-624", "label-196")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
