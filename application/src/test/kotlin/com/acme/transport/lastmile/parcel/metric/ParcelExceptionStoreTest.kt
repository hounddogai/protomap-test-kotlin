package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelExceptionStore()
        val item = newParcelException("code-621", "label-842")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
