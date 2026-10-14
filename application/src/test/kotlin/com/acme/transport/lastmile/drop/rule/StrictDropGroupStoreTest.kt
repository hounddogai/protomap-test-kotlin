package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDropGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDropGroupStore()
        val item = newStrictDropGroup("code-352", "title-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
