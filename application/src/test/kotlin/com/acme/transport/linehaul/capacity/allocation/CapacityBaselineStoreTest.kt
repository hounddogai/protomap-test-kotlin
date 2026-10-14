package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityBaselineStore()
        val item = newCapacityBaseline("code-986", "title-343")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
