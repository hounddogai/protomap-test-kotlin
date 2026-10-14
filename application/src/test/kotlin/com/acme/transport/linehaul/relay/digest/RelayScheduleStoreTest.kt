package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayScheduleStore()
        val item = newRelaySchedule("code-171", "label-498")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
