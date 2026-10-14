package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerPlanStore()
        val item = newTrailerPlan("code-915", "title-667")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
