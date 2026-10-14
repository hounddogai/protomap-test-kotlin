package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanBatchStore()
        val item = newServicePlanBatch("code-636", "label-123")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
