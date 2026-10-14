package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRestBreakOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRestBreakOverrideStore()
        val item = newWeightedRestBreakOverride("code-129", "label-87")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
