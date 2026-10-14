package com.acme.transport.lastmile.drop.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropTierStore()
        val item = newDropTier("code-845", "label-598")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
