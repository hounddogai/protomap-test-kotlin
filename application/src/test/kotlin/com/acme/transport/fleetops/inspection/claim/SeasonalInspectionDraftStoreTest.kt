package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalInspectionDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalInspectionDraftStore()
        val item = newSeasonalInspectionDraft("code-486", "title-711")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
