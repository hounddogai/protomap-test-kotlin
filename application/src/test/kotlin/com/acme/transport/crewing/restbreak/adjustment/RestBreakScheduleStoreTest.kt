package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakScheduleStore()
        val item = newRestBreakSchedule("code-774", "label-33")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
