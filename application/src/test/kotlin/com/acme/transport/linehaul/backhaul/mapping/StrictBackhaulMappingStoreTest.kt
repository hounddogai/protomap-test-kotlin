package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictBackhaulMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictBackhaulMappingStore()
        val item = newStrictBackhaulMapping("code-648", "title-149")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
