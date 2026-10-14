package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDefectDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDefectDigestStore()
        val item = newBatchedDefectDigest("code-218", "label-267")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
