package com.acme.transport.safety.nearmiss.missrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledNearMissRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledNearMissRecordStore()
        val item = newScheduledNearMissRecord("code-57", "label-568")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
