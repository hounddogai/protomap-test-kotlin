package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaPlanStore()
        val item = newServiceAreaPlan("code-52", "title-246")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
