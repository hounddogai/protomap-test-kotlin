package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCutoffAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCutoffAssignmentStore()
        val item = newLenientCutoffAssignment("code-142", "name-748")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
