package com.acme.transport.customs.clearance.seasonalclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalClearanceOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalClearanceOverrideStore()
        val item = newGlobalClearanceOverride("code-278", "title-430")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
