package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCoachingReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCoachingReservationStore()
        val item = newCappedCoachingReservation("code-230", "title-808")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
