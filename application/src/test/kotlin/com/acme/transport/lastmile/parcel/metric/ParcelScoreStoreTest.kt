package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelScoreStore()
        val item = newParcelScore("code-842", "title-544")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
