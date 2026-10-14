package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanExceptionStore()
        val item = newServicePlanException("code-134", "label-283")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
