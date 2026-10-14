package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTenderEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTenderEntryStore()
        val item = newPrimaryTenderEntry("code-811", "title-818")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
