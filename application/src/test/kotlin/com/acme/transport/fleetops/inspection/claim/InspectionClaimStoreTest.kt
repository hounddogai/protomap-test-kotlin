package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionClaimStore()
        val item = newInspectionClaim("code-295", "title-807")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
