package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingInspectionRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingInspectionRollupStore()
        val item = newPendingInspectionRollup("code-527", "label-321")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
