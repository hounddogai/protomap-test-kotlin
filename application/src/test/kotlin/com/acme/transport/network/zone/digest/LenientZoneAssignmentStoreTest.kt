package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientZoneAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientZoneAssignmentStore()
        val item = newLenientZoneAssignment("code-749", "title-203")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
