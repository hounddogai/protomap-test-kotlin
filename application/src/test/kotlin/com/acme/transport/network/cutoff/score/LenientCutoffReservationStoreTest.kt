package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCutoffReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCutoffReservationStore()
        val item = newLenientCutoffReservation("code-852", "title-762")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
