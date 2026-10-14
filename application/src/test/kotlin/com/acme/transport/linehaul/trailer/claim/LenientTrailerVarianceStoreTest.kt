package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTrailerVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTrailerVarianceStore()
        val item = newLenientTrailerVariance("code-511", "name-399")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
