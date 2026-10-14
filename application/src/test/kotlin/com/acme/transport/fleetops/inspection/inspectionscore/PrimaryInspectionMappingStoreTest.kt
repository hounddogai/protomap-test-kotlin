package com.acme.transport.fleetops.inspection.inspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryInspectionMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryInspectionMappingStore()
        val item = newPrimaryInspectionMapping("code-730", "label-319")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
