package com.acme.transport.crewing.swap.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapDigestStore()
        val item = newSwapDigest("code-892", "label-437")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
