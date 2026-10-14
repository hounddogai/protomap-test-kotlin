package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanRecordStore()
        val item = newLanePlanRecord("code-288", "title-611")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
