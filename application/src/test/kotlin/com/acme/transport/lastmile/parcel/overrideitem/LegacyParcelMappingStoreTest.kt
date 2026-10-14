package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyParcelMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyParcelMappingStore()
        val item = newLegacyParcelMapping("code-386", "label-530")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
