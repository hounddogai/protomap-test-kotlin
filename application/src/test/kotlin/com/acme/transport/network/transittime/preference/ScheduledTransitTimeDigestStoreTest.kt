package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTransitTimeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTransitTimeDigestStore()
        val item = newScheduledTransitTimeDigest("code-913", "title-73")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
