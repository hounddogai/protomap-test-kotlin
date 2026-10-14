package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderPreferenceStore()
        val item = newTenderPreference("code-672", "label-819")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
