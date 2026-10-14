package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTransitTimeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTransitTimeBatchStore()
        val item = newScheduledTransitTimeBatch("code-754", "name-424")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
