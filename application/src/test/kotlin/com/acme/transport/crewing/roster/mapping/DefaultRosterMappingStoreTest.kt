package com.acme.transport.crewing.roster.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRosterMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRosterMappingStore()
        val item = newDefaultRosterMapping("code-86", "label-247")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
