package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityScoreStore()
        val item = newCapacityScore("code-779", "label-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
