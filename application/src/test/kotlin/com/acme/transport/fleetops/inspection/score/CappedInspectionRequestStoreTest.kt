package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedInspectionRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedInspectionRequestStore()
        val item = newCappedInspectionRequest("code-305", "label-933")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
