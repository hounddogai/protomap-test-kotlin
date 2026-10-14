package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelAdjustmentStore()
        val item = newParcelAdjustment("code-577", "name-495")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
