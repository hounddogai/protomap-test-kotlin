package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTrailerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTrailerOverrideStore()
        val item = newScheduledTrailerOverride("code-329", "name-57")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
