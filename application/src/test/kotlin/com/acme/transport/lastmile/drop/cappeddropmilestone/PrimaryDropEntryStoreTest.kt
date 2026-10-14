package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDropEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDropEntryStore()
        val item = newPrimaryDropEntry("code-574", "title-713")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
