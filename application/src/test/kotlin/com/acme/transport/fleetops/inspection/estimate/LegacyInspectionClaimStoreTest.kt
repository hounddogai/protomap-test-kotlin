package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionClaimStore()
        val item = newLegacyInspectionClaim("code-949", "title-797")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
