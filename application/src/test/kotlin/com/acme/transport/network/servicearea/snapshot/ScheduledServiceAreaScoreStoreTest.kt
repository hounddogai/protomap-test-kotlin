package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledServiceAreaScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledServiceAreaScoreStore()
        val item = newScheduledServiceAreaScore("code-233", "name-301")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
