package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapMappingStore()
        val item = newSwapMapping("code-188", "label-923")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
