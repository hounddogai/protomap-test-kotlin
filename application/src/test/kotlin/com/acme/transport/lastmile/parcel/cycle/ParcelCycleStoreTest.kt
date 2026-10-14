package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelCycleStore()
        val item = newParcelCycle("code-63", "title-782")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
