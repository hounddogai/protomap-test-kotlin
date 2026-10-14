package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDefectProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDefectProfileStore()
        val item = newBatchedDefectProfile("code-757", "name-557")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
