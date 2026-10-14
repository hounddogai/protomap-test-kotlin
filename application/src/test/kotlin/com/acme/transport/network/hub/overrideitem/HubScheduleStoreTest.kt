package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubScheduleStore()
        val item = newHubSchedule("code-100", "title-895")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
