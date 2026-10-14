package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaExceptionStore()
        val item = newServiceAreaException("code-885", "title-49")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
