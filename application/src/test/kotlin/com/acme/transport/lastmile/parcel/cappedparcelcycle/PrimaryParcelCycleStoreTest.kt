package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryParcelCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryParcelCycleStore()
        val item = newPrimaryParcelCycle("code-382", "name-202")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
