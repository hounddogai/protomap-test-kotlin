package com.acme.transport.customs.hold.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultHoldScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultHoldScheduleStore()
        val item = newDefaultHoldSchedule("code-524", "title-272")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
