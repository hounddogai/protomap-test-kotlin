package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireDraftStore()
        val item = newTireDraft("code-138", "label-776")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
