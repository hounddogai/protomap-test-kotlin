package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCoachingAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCoachingAdjustmentStore()
        val item = newGlobalCoachingAdjustment("code-611", "title-355")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
