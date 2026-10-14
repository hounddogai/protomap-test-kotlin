package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedServicePlanRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedServicePlanRecordStore()
        val item = newBatchedServicePlanRecord("code-803", "label-810")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
