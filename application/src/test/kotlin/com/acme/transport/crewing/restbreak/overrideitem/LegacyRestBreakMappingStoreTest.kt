package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRestBreakMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRestBreakMappingStore()
        val item = newLegacyRestBreakMapping("code-834", "title-469")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
