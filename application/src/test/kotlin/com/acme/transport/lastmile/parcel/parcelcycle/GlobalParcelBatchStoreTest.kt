package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalParcelBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalParcelBatchStore()
        val item = newGlobalParcelBatch("code-363", "name-235")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
