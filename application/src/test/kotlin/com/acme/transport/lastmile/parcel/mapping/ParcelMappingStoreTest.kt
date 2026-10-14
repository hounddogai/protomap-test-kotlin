package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelMappingStore()
        val item = newParcelMapping("code-884", "title-992")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
