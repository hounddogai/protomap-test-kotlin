package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDefectClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDefectClaimStore()
        val item = newPrimaryDefectClaim("code-401", "title-979")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
