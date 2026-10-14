package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredParcelPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredParcelPreferenceStore()
        val item = newTieredParcelPreference("code-829", "title-514")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
