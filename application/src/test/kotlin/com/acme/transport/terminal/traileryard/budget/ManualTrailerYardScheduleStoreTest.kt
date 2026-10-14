package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerYardScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerYardScheduleStore()
        val item = newManualTrailerYardSchedule("code-986", "label-60")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
