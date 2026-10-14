package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDwellGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDwellGroupStore()
        val item = newAdaptiveDwellGroup("code-869", "name-433")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
