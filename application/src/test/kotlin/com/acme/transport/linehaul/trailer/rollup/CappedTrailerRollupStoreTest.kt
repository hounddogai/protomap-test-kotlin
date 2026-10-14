package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTrailerRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTrailerRollupStore()
        val item = newCappedTrailerRollup("code-580", "name-172")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
