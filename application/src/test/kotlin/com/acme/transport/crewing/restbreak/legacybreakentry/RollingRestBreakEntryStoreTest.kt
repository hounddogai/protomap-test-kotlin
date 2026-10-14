package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRestBreakEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRestBreakEntryStore()
        val item = newRollingRestBreakEntry("code-46", "label-716")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
