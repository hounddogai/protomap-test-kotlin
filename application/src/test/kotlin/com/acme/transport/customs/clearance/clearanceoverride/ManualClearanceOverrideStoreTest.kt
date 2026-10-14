package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualClearanceOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualClearanceOverrideStore()
        val item = newManualClearanceOverride("code-566", "name-588")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
