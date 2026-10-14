package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDwellCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDwellCycleStore()
        val item = newRollingDwellCycle("code-707", "title-293")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
