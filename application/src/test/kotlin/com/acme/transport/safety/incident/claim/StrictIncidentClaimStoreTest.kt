package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictIncidentClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictIncidentClaimStore()
        val item = newStrictIncidentClaim("code-25", "title-190")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
