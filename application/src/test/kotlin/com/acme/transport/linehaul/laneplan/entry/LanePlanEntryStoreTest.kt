package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanEntryStore()
        val item = newLanePlanEntry("code-874", "name-632")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
