package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneDraftStore()
        val item = newZoneDraft("code-802", "label-662")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
