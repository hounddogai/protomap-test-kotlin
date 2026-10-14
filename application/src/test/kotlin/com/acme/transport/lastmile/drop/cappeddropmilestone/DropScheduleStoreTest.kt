package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropScheduleStore()
        val item = newDropSchedule("code-406", "name-366")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
