package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientParcelSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientParcelSlotStore()
        val item = newLenientParcelSlot("code-633", "name-861")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
