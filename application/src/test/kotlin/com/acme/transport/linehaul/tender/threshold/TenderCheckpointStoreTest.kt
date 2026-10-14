package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderCheckpointStore()
        val item = newTenderCheckpoint("code-733", "title-595")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
