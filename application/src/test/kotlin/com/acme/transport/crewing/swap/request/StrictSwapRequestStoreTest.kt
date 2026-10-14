package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictSwapRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictSwapRequestStore()
        val item = newStrictSwapRequest("code-154", "title-807")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
