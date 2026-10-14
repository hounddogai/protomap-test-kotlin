package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedInspectionClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedInspectionClaimStore()
        val item = newBatchedInspectionClaim("code-244", "name-284")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
