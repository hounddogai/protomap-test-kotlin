package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapSegmentStore()
        val item = newSwapSegment("code-148", "title-231")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
