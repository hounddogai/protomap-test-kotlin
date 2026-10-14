package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualBackhaulClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualBackhaulClaimStore()
        val item = newManualBackhaulClaim("code-312", "label-465")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
