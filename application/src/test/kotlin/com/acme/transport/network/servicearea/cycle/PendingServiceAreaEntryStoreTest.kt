package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingServiceAreaEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingServiceAreaEntryStore()
        val item = newPendingServiceAreaEntry("code-146", "title-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
