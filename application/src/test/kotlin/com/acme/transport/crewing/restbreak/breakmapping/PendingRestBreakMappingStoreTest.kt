package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRestBreakMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRestBreakMappingStore()
        val item = newPendingRestBreakMapping("code-646", "title-838")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
