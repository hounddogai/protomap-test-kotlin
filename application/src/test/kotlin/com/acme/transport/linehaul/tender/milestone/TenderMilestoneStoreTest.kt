package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderMilestoneStore()
        val item = newTenderMilestone("code-298", "label-34")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
