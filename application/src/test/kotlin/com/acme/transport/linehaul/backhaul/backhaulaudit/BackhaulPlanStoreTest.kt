package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulPlanStore()
        val item = newBackhaulPlan("code-281", "title-571")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
