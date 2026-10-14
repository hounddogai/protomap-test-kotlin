package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityRecordStore()
        val item = newAvailabilityRecord("code-936", "title-949")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
