package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakCycleStore()
        val item = newRestBreakCycle("code-211", "title-28")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
