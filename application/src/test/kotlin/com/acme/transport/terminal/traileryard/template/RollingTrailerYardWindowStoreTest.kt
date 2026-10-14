package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTrailerYardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTrailerYardWindowStore()
        val item = newRollingTrailerYardWindow("code-45", "label-59")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
