package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTenderSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTenderSlotStore()
        val item = newFallbackTenderSlot("code-855", "title-658")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
