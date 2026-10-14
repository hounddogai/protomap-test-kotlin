package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTrailerTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTrailerTemplateStore()
        val item = newTieredTrailerTemplate("code-35", "label-144")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
