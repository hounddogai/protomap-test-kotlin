package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDropSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDropSlotStore()
        val item = newLegacyDropSlot("code-564", "title-898")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
