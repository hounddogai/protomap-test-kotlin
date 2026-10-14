package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTenderPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTenderPlanStore()
        val item = newFallbackTenderPlan("code-806", "name-239")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
