package com.acme.transport.crewing.availability.primaryavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAvailabilityDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAvailabilityDraftStore()
        val item = newManualAvailabilityDraft("code-562", "title-871")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
