package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTrailerEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTrailerEntryStore()
        val item = newBatchedTrailerEntry("code-709", "label-102")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
