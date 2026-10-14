package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCapacityExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCapacityExceptionStore()
        val item = newDefaultCapacityException("code-368", "name-930")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
