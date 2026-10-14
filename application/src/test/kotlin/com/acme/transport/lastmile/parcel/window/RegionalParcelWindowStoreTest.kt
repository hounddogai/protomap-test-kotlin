package com.acme.transport.lastmile.parcel.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalParcelWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalParcelWindowStore()
        val item = newRegionalParcelWindow("code-771", "title-412")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
