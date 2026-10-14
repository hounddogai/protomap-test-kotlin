package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictSwapVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictSwapVarianceStore()
        val item = newStrictSwapVariance("code-940", "title-519")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
