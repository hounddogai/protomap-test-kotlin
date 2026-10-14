package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelOverrideStore()
        val item = newParcelOverride("code-481", "title-384")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
