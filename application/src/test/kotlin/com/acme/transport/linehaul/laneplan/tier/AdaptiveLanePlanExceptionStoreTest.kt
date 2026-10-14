package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLanePlanExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLanePlanExceptionStore()
        val item = newAdaptiveLanePlanException("code-206", "title-636")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
