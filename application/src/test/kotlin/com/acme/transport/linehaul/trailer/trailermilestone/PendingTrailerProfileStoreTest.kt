package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTrailerProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTrailerProfileStore()
        val item = newPendingTrailerProfile("code-216", "name-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
