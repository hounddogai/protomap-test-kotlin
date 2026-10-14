package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissReservationStore()
        val item = newNearMissReservation("code-110", "label-503")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
