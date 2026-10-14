package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTrailerYardVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTrailerYardVarianceStore()
        val item = newRollingTrailerYardVariance("code-831", "name-415")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
