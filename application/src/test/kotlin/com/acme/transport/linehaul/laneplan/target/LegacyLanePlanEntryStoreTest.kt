package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyLanePlanEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyLanePlanEntryStore()
        val item = newLegacyLanePlanEntry("code-867", "label-977")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
