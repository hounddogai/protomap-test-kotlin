package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCutoffReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCutoffReservationStore()
        val item = newGlobalCutoffReservation("code-971", "name-113")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
