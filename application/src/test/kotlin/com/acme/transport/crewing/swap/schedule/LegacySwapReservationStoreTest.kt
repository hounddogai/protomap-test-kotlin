package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacySwapReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacySwapReservationStore()
        val item = newLegacySwapReservation("code-84", "label-397")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
