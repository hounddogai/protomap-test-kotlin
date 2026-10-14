package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCutoffReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCutoffReservationStore()
        val item = newTieredCutoffReservation("code-969", "name-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
