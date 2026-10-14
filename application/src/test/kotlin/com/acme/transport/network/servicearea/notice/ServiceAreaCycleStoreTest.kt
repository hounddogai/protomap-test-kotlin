package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaCycleStore()
        val item = newServiceAreaCycle("code-877", "name-252")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
