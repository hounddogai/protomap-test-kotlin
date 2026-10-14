package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTenderCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTenderCycleStore()
        val item = newScheduledTenderCycle("code-409", "title-439")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
