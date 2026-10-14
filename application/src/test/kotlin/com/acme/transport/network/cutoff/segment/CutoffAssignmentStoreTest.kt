package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffAssignmentStore()
        val item = newCutoffAssignment("code-178", "title-285")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
