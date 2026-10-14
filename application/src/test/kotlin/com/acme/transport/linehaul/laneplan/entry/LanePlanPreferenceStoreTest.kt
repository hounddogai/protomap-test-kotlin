package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanPreferenceStore()
        val item = newLanePlanPreference("code-738", "label-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
