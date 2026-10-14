package com.acme.transport.lastmile.drop.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropProfileStore()
        val item = newDropProfile("code-614", "label-632")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
