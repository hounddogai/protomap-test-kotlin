package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeDraftStore()
        val item = newTransitTimeDraft("code-865", "name-507")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
