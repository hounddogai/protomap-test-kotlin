package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellRollupStore()
        val item = newDwellRollup("code-180", "title-301")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
