package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultParcelTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultParcelTierStore()
        val item = newDefaultParcelTier("code-222", "label-668")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
