package com.acme.transport.fleetops.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingInspectionBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingInspectionBundleStore()
        val item = newPendingInspectionBundle("code-179", "title-80")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
