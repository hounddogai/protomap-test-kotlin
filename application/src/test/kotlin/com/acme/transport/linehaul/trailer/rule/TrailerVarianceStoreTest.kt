package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerVarianceStore()
        val item = newTrailerVariance("code-389", "title-59")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
