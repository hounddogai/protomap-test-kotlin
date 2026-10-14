package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTenderProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTenderProfileStore()
        val item = newStrictTenderProfile("code-849", "label-856")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
