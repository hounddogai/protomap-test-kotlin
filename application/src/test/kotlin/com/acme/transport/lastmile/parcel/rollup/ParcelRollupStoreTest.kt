package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelRollupStore()
        val item = newParcelRollup("code-856", "label-690")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
