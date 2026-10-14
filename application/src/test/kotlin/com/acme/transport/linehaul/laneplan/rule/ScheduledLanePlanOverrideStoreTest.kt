package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLanePlanOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLanePlanOverrideStore()
        val item = newScheduledLanePlanOverride("code-90", "name-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
