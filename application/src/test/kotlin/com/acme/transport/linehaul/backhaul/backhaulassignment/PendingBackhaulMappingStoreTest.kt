package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingBackhaulMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingBackhaulMappingStore()
        val item = newPendingBackhaulMapping("code-491", "name-568")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
