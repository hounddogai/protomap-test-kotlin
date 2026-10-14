package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLanePlanDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLanePlanDraftStore()
        val item = newRollingLanePlanDraft("code-364", "name-819")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
