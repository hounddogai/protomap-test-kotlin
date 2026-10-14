package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelProfileStore()
        val item = newParcelProfile("code-179", "name-263")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
