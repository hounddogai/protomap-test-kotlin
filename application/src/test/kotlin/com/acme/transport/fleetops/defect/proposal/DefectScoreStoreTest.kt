package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectScoreStore()
        val item = newDefectScore("code-172", "label-430")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
