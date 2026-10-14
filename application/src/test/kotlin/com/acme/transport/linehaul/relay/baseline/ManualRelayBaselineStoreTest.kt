package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRelayBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRelayBaselineStore()
        val item = newManualRelayBaseline("code-385", "name-614")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
