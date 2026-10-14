package com.acme.transport.network.servicearea.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaAllocationStore()
        val item = newServiceAreaAllocation("code-181", "name-421")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
