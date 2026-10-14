package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTireDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTireDraftStore()
        val item = newLegacyTireDraft("code-219", "title-582")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
