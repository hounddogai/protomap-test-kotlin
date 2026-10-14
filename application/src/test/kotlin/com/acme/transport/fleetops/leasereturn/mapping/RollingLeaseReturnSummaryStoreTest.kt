package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLeaseReturnSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLeaseReturnSummaryStore()
        val item = newRollingLeaseReturnSummary("code-497", "title-616")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
