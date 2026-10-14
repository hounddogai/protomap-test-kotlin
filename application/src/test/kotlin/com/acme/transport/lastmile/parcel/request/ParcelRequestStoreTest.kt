package com.acme.transport.lastmile.parcel.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelRequestStore()
        val item = newParcelRequest("code-260", "label-253")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
