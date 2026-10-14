package com.acme.transport.emissions.idling.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingPlanStore()
        val item = newIdlingPlan("code-138", "title-193")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
