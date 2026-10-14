package com.acme.transport.linehaul.laneplan.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLanePlanRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLanePlanRecordStore()
        val item = newScheduledLanePlanRecord("code-882", "label-956")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
