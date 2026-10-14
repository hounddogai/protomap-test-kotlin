package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingParcelAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingParcelAssignmentStore()
        val item = newPendingParcelAssignment("code-152", "title-526")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
