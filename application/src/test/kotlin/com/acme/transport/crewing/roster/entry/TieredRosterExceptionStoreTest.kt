package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRosterExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRosterExceptionStore()
        val item = newTieredRosterException("code-892", "title-114")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
