package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredHoldDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredHoldDraftStore()
        val item = newTieredHoldDraft("code-67", "label-629")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
