package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaProfileStore()
        val item = newServiceAreaProfile("code-86", "name-62")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
