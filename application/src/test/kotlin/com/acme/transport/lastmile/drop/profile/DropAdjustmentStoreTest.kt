package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropAdjustmentStore()
        val item = newDropAdjustment("code-495", "title-437")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
