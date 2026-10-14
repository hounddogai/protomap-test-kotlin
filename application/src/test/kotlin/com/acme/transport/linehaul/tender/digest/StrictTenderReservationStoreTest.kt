package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTenderReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTenderReservationStore()
        val item = newStrictTenderReservation("code-402", "title-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
