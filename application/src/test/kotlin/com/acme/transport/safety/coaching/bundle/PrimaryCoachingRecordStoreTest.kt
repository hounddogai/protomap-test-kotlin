package com.acme.transport.safety.coaching.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCoachingRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCoachingRecordStore()
        val item = newPrimaryCoachingRecord("code-677", "title-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
