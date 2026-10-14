package com.acme.transport.linehaul.laneplan.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingLanePlanItemStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingLanePlanItemStore()
        val item = newPendingLanePlanItem("code-72", "name-162")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
