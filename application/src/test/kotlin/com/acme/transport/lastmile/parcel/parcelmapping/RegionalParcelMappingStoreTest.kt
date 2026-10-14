package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalParcelMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalParcelMappingStore()
        val item = newRegionalParcelMapping("code-180", "name-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
