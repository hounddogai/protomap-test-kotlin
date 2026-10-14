package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRestBreakExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRestBreakExceptionStore()
        val item = newTieredRestBreakException("code-378", "label-763")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
