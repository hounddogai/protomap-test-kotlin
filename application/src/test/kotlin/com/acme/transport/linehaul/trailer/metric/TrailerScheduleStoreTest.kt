package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerScheduleStore()
        val item = newTrailerSchedule("code-483", "name-776")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
