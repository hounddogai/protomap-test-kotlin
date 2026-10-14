package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRelayScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRelayScheduleStore()
        val item = newCappedRelaySchedule("code-836", "title-301")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
