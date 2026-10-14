package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelWindowStore()
        val item = newParcelWindow("code-421", "name-776")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
