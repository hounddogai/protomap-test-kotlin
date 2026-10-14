package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRestBreakVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRestBreakVarianceStore()
        val item = newDefaultRestBreakVariance("code-438", "title-642")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
