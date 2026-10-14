package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRestBreakExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRestBreakExceptionStore()
        val item = newAdaptiveRestBreakException("code-149", "label-422")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
