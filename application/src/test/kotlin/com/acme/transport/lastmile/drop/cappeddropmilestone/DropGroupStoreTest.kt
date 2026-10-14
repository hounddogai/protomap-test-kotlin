package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropGroupStore()
        val item = newDropGroup("code-168", "name-996")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
