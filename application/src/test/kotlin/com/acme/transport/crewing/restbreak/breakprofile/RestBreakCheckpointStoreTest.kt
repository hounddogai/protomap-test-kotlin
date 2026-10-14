package com.acme.transport.crewing.restbreak.breakprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakCheckpointStore()
        val item = newRestBreakCheckpoint("code-134", "title-913")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
