package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTrailerVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTrailerVarianceStore()
        val item = newFallbackTrailerVariance("code-889", "name-943")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
