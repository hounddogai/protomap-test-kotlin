package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionManifestStore()
        val item = newInspectionManifest("code-391", "name-84")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
