package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakVarianceStore()
        val item = newRestBreakVariance("code-761", "name-565")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
