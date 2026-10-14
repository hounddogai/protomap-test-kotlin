package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTrailerRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTrailerRequestStore()
        val item = newAdaptiveTrailerRequest("code-915", "name-803")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
