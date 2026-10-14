package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalSwapOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalSwapOverrideStore()
        val item = newGlobalSwapOverride("code-345", "label-967")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
