package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRestBreakEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRestBreakEstimateStore()
        val item = newLegacyRestBreakEstimate("code-203", "title-533")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
