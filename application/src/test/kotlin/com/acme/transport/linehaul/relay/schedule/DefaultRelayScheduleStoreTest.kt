package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRelayScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRelayScheduleStore()
        val item = newDefaultRelaySchedule("code-371", "title-85")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
