package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingParcelMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingParcelMappingStore()
        val item = newRollingParcelMapping("code-600", "title-180")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
