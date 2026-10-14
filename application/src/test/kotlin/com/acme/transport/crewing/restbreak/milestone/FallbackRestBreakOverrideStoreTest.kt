package com.acme.transport.crewing.restbreak.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRestBreakOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRestBreakOverrideStore()
        val item = newFallbackRestBreakOverride("code-275", "label-722")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
