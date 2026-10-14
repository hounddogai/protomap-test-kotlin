package com.acme.transport.crewing.restbreak.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRestBreakScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRestBreakScheduleStore()
        val item = newPendingRestBreakSchedule("code-181", "name-296")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
