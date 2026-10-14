package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingInspectionOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingInspectionOverrideStore()
        val item = newPendingInspectionOverride("code-154", "title-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
