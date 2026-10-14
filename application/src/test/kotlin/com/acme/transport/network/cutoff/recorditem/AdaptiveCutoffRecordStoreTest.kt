package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCutoffRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCutoffRecordStore()
        val item = newAdaptiveCutoffRecord("code-223", "title-530")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
