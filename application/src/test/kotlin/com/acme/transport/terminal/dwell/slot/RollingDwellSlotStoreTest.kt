package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDwellSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDwellSlotStore()
        val item = newRollingDwellSlot("code-555", "title-191")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
