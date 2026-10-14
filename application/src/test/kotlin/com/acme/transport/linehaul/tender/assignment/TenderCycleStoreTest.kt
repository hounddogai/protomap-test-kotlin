package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderCycleStore()
        val item = newTenderCycle("code-547", "title-540")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
