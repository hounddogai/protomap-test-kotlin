package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLeaseReturnScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLeaseReturnScheduleStore()
        val item = newAdaptiveLeaseReturnSchedule("code-622", "label-105")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
