package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityDigestStore()
        val item = newCapacityDigest("code-431", "title-776")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
