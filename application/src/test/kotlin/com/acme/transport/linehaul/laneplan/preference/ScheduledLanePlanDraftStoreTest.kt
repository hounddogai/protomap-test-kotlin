package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLanePlanDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLanePlanDraftStore()
        val item = newScheduledLanePlanDraft("code-298", "title-322")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
