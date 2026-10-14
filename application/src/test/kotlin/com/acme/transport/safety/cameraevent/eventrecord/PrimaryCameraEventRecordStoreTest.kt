package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCameraEventRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCameraEventRecordStore()
        val item = newPrimaryCameraEventRecord("code-296", "title-843")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
