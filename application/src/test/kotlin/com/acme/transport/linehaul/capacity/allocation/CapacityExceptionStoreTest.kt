package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityExceptionStore()
        val item = newCapacityException("code-840", "name-898")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
