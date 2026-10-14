package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityVarianceStore()
        val item = newCapacityVariance("code-863", "label-375")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
