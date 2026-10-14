package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalLeaseReturnCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalLeaseReturnCycleStore()
        val item = newRegionalLeaseReturnCycle("code-98", "label-905")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
