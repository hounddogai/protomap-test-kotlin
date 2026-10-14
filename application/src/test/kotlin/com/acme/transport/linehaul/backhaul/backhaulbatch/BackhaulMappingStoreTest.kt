package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulMappingStore()
        val item = newBackhaulMapping("code-32", "label-732")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
