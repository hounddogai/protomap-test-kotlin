package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffRecordStore()
        val item = newCutoffRecord("code-993", "label-452")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
