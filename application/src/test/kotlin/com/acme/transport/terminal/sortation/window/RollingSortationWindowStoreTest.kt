package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSortationWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSortationWindowStore()
        val item = newRollingSortationWindow("code-727", "name-241")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
