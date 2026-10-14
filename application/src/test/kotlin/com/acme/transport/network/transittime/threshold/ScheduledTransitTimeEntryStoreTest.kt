package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTransitTimeEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTransitTimeEntryStore()
        val item = newScheduledTransitTimeEntry("code-239", "label-250")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
