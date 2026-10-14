package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingSummaryStore()
        val item = newIdlingSummary("code-873", "title-105")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
