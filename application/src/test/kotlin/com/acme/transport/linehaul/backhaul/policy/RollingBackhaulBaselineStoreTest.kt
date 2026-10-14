package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBackhaulBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBackhaulBaselineStore()
        val item = newRollingBackhaulBaseline("code-926", "title-256")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
