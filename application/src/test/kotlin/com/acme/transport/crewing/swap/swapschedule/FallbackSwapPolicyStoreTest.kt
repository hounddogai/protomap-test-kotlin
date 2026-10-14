package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackSwapPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackSwapPolicyStore()
        val item = newFallbackSwapPolicy("code-82", "label-886")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
